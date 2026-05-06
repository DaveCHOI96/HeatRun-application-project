package com.main.heatrun.domain.running.service;

import com.main.heatrun.domain.entity.*;
import com.main.heatrun.domain.repository.*;
import com.main.heatrun.domain.running.dto.CompleteRunningRequest;
import com.main.heatrun.domain.running.dto.RoutePointRequest;
import com.main.heatrun.domain.running.dto.RunningSessionResponse;
import com.main.heatrun.domain.running.dto.StartRunningRequest;
import com.main.heatrun.global.enums.ExpSourceType;
import com.main.heatrun.global.enums.RecordType;
import com.main.heatrun.global.enums.RunningStatus;
import com.main.heatrun.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningService {

    private final RunningSessionRepository runningSessionRepository;
    private final RoutePointRepository routePointRepository;
    private final GhostRecordRepository ghostRecordRepository;
    private final UserLevelRepository userLevelRepository;
    private final ExpLogRepository expLogRepository;
    private final UserRepository userRepository;

    // GPS 좌표 생성용 (SRID 4326 = WGS84)
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    // 러닝 시작
    @Transactional
    public RunningSessionResponse startRunning(UUID userId,
                                               StartRunningRequest request) {
        User user = findActiveUser(userId);

        runningSessionRepository
                .findByUserIdAndStatus(userId,
                        RunningStatus.IN_PROGRESS)
                .ifPresent(session -> {
                    throw new BusinessException("이미 진행 중인 러닝이 있습니다.", HttpStatus.CONFLICT);
                });

        RunningSession session;

        // 고스트 러닝 여부 분기
        if (request.ghostSessionId() != null) {
            RunningSession ghostSession = runningSessionRepository
                    .findById(request.ghostSessionId())
                    .orElseThrow(() -> new BusinessException("고스트 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

            session = RunningSession.startWithGhost(
                    user,
                    request.weatherCondition(),
                    ghostSession
            );
        } else {
            session = RunningSession.start(user, request.weatherCondition());
        }

        runningSessionRepository.save(session);

        log.info("러닝 시작: userId={}, sessionId={}, ghost={}",
                userId, session.getId(), request.ghostSessionId() != null);

        return RunningSessionResponse.from(session);
    }

    // GPS 좌표 저장
    @Transactional
    public void saveRoutePoint(UUID userId, UUID sessionId, RoutePointRequest request) {
        RunningSession session = findMyActiveSession(userId, sessionId);

        // 위도/경도 → PostGIS Point 변환
        // Coordinate(x, y) = Coordinate(경도, 위도) 순서 주의
        Point location = geometryFactory.createPoint(
                new Coordinate(request.longitude(), request.latitude())
        );

        RoutePoint routePoint = RoutePoint.of(
                session,
                location,
                request.altitude(),
                request.speed(),
                request.sequenceNumber()
        );

        routePointRepository.save(routePoint);
    }

    // 러닝 일시정지
    @Transactional
    public RunningSessionResponse pauseRunning(UUID userId, UUID sessionId) {
        RunningSession session = findMyActiveSession(userId, sessionId);
        session.pause();

        log.info("러닝 일시정지: sessionId={}", sessionId);
        return RunningSessionResponse.from(session);
    }

    @Transactional
    public RunningSessionResponse resumeRunning(UUID userId, UUID sessionId) {
        RunningSession session = findMySession(userId, sessionId);

        if (session.getStatus() != RunningStatus.PAUSED) {
            throw new BusinessException("일시정지 상태가 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        session.resume();

        log.info("러닝 재개: sessionId={}", sessionId);
        return RunningSessionResponse.from(session);
    }

    // 러닝 완료
    @Transactional
    public RunningSessionResponse completeRunning(UUID userId, UUID sessionId, CompleteRunningRequest request) {
        RunningSession session = findMySession(userId, sessionId);

        if (session.isCompleted()) {
            throw new BusinessException("이미 완료된 러닝입니다.", HttpStatus.BAD_REQUEST);
        }

        // 러닝 완료 처리
        session.complete(
                request.totalDistanceKm(),
                request.durationSeconds(),
                request.avgPace(),
                request.caloriesBurned()
        );

        // EXP 지급 (1km당 10 EXP)
        int expEarned = (int) (request.totalDistanceKm() * 10);
        grantExp(userId, expEarned, ExpSourceType.RUNNING, session.getId());

        // PB체크 및 고스트 자동 등록
        checkAndCreatePersonalBest(userId, session);

        log.info("러닝 완료: sessionId={}, distance={}km, exp={}",
                sessionId, request.totalDistanceKm(), expEarned);

        return RunningSessionResponse.from(session);
    }

    // 러닝 기록 조회
    @Transactional(readOnly = true)
    public List<RunningSessionResponse> getMyRunningSessions(UUID userId) {
        return runningSessionRepository
                .findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(RunningSessionResponse::from)
                .collect(Collectors.toList());
    }

    // 러닝 상세 조회
    @Transactional(readOnly = true)
    public RunningSessionResponse getRunningSession(UUID userId, UUID sessionId) {
        return RunningSessionResponse.from(findMySession(userId, sessionId));
    }

    // GPS 경로 조회 (고스트 재현용)
    @Transactional(readOnly = true)
    public List<RoutePointRequest> getRoutePoints(UUID sessionId) {
        return routePointRepository
                .findBySessionIdOrderBySequenceNumberAsc(sessionId)
                .stream()
                .map(point -> new RoutePointRequest(
                        point.getLocation().getY(),
                        point.getLocation().getX(),
                        point.getAltitude(),
                        point.getSpeed(),
                        point.getSequenceNumber()
                ))
                .collect(Collectors.toList());
    }

    // ---- EXP 지급 공통 메서드 ----
    private void grantExp(UUID userId, int expEarned, ExpSourceType sourceType, UUID sourceId) {

        // EXP 로그 저장
        User user = findActiveUser(userId);
        ExpLog expLog = ExpLog.of(user, expEarned, sourceType,sourceId);
        expLogRepository.save(expLog);

        // 유저 레벨 업데이트
        userLevelRepository.findByUserId(userId)
                .ifPresent(level -> level.addExp(expEarned));

        log.info("EXP 지급: userId={}, exp={}, source={}", userId, expEarned, sourceType);
    }

    // PB 체크 및 고스트 자동 등록
    private void checkAndCreatePersonalBest(UUID userId, RunningSession session) {

        // 현재 PB 조회
        ghostRecordRepository
                .findByOwnerUserIdAndRecordType(
                        userId, RecordType.PERSONAL_BEST)
                .ifPresentOrElse(
                        existingPb -> {
                            // 기존 PB보다 빠르면 갱신
                            if (session.getAvgPace() < existingPb.getDurationSeconds()
                                / (existingPb.getTotalDistanceKm() * 60)) {
                                existingPb.makePrivate();
                                ghostRecordRepository.save(
                                        GhostRecord.createPersonalBest(
                                                session.getUser(), session));
                                log.info("PB 갱신: userId={}", userId);
                            }
                        },
                        () -> {
                            // PB 없으면 최초 등록
                            ghostRecordRepository.save(
                                    GhostRecord.createPersonalBest(
                                            session.getUser(), session));
                            log.info("PB 최초 등록: userId={}", userId);
                        }
                );
                
    }

    // ---- 공통 메서드 ----

    // 진행 중인 내 세션 조회
    private RunningSession findMyActiveSession(UUID userId, UUID sessionId) {
        RunningSession session = findMySession(userId, sessionId);

        if (session.getStatus() == RunningStatus.COMPLETED) {
            throw new BusinessException("이미 완료된 러닝입니다.", HttpStatus.BAD_REQUEST);
        }
        return session;
    }

    // 내 세션 조회 (완료 포함)
    private RunningSession findMySession(UUID userId, UUID sessionId) {
        RunningSession session = runningSessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new BusinessException("러닝 세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 본인 세션인지 확인
        if (!session.getUser().getId().equals(userId)) {
            throw new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        return session;
    }

    // 활성 유저 조회
    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException(
                        "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
