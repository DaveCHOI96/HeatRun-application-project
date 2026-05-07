package com.main.heatrun.domain.crew.service;

import com.main.heatrun.domain.crew.dto.*;
import com.main.heatrun.domain.entity.Crew;
import com.main.heatrun.domain.entity.CrewCheer;
import com.main.heatrun.domain.entity.CrewMember;
import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.*;
import com.main.heatrun.global.enums.RunningStatus;
import com.main.heatrun.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewCheerRepository crewCheerRepository;
    private final UserRepository userRepository;
    private final RunningSessionRepository runningSessionRepository;

    // 응원 제한 시간 - 10분에 1회
    private static final int CHEER_LIMIT_MINUTES = 10;

    // 크루 생성
    @Transactional
    public CrewResponse createCrew(UUID userId, CreateCrewRequest request) {
        User leader = findActiveUser(userId);

        // 크루 이름 중복 체크
        if (crewRepository.existsByName(request.name())) {
            throw new BusinessException("이미 사용 중인 크루 이름입니다.", HttpStatus.CONFLICT);
        }

        // 크루 생성 조건 - 러닝 미션 1회 이상 완료
        long completedRuns = runningSessionRepository
                .countByUserIdAndStatus(
                        userId, RunningStatus.COMPLETED);
        if (completedRuns < 1) {
            throw new BusinessException("크루 생성을 위해 러닝 1회 이상 완료해야 합니다.", HttpStatus.FORBIDDEN);
        }

        Crew crew = Crew.create(
                leader,
                request.name(),
                request.description(),
                request.visibility()
        );
        crewRepository.save(crew);

        // 리더를 크루 멤버로 자동 등록
        CrewMember leaderMember = CrewMember.joinAsLeader(crew, leader);
        crewMemberRepository.save(leaderMember);

        log.info("크루 생성: crewId={}, leader={}", crew.getId(), userId);
        return CrewResponse.from(crew);
    }

    // 크루 검색
    @Transactional(readOnly = true)
    public Page<CrewResponse> searchCrews(String keyword, Pageable pageable) {
        return crewRepository
                .searchPublicCrews(keyword, pageable)
                .map(CrewResponse::from);
    }

    // 크루 상세 조회
    @Transactional(readOnly = true)
    public CrewResponse getCrew(UUID crewId) {
        return CrewResponse.from(findCrew(crewId));
    }

    // 내 크루 목록
    @Transactional(readOnly = true)
    public List<CrewResponse> getMyCrews(UUID userId) {
        return crewMemberRepository.findByUserId(userId)
                .stream()
                .map(CrewMember::getCrew)
                .map(CrewResponse::from)
                .collect(Collectors.toList());
    }

    // 크루 멤버 목록
    @Transactional(readOnly = true)
    public List<CrewMemberResponse> getCrewMembers(UUID crewId) {
        return crewMemberRepository.findByCrewId(crewId)
                .stream()
                .map(CrewMemberResponse::from)
                .collect(Collectors.toList());
    }

    // 크루 가입
    @Transactional
    public CrewResponse joinCrew(UUID userId, UUID crewId) {
        User user = findActiveUser(userId);
        Crew crew = findCrew(crewId);

        // 이미 가입된 크루인지 확인
        if (crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new BusinessException("이미 가입된 크루입니다.", HttpStatus.CONFLICT);
        }

        // 비공개 크루 가입 제한
        if (!crew.isPublic()) {
            throw new BusinessException("초대 전용 크루입니다.", HttpStatus.FORBIDDEN);
        }

        // 정원 초과 체크
        if (crew.isFull()) {
            throw new BusinessException("크루 정원이 가득 찼습니다.", HttpStatus.CONFLICT);
        }

        // 멤버 추가
        CrewMember member = CrewMember.join(crew, user);
        crewMemberRepository.save(member);

        // 크루 멤버 수 증가
        crew.increaseMemberCount();
        log.info("크루 가입: crewId={}, userId={}", crewId, userId);
        return CrewResponse.from(crew);
    }

    // 크루 탈퇴
    @Transactional
    public void leaveCrew(UUID userId, UUID crewId) {
        Crew crew = findCrew(crewId);

        CrewMember member = crewMemberRepository
                .findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new BusinessException("크루 멤버가 아닙니다.", HttpStatus.NOT_FOUND));

        // 리더는 바로 탈퇴 불가 - 리더 위임 후 탈퇴 가능
        if (member.isLeader()) {
            throw new BusinessException("리더는 탈퇴할 수 없습니다. 리더를 위임한 후 탈퇴해주세요.",
                    HttpStatus.BAD_REQUEST);
        }

        crewMemberRepository.delete(member);
        crew.decreaseMemberCount();
        log.info("크루 탈퇴: crewId={}, userId={}", crewId, userId);
    }

    // 크루 정보 수정
    @Transactional
    public CrewResponse updateCrew(UUID userId, UUID crewId, UpdateCrewRequest request) {
        Crew crew = findCrew(crewId);

        // 리더만 수정 가능
        checkIsLeader(userId, crewId);

        // 이름 변경 시 중복 체크 (현재 이름 제외)
        if (!crew.getName().equals(request.name()) && crewRepository.existsByName(request.name())) {
            throw new BusinessException("이미 사용 중인 크루 이름입니다.", HttpStatus.CONFLICT);
        }
        crew.update(request.name(), request.description());
        log.info("크루 정보 수정: crewId={}", crewId);
        return CrewResponse.from(crew);
    }

    // 리더 위임
    @Transactional
    public void delegateLeader(UUID userId, UUID crewId, UUID targetUserId) {
        // 현재 리더 확인
        checkIsLeader(userId, crewId);

        // 새 리더 멤버 조회
        CrewMember newLeaderMember = crewMemberRepository
                .findByCrewIdAndUserId(crewId, targetUserId)
                .orElseThrow(() -> new BusinessException("대상 유저가 크루 멤버가 아닙니다.", HttpStatus.NOT_FOUND));

        // 기존 리더 -> 일반 멤버로 강등
        CrewMember currentLeaderMember = crewMemberRepository
                .findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new BusinessException("크루 멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 크루 리더 변경
        Crew crew = findCrew(crewId);
        currentLeaderMember.promoteToLeader(); // 기존 리더 역할 유지용 메서드
        newLeaderMember.promoteToLeader();

        log.info("리더 위임: crewId={}, from={}, to={}", crewId, userId, targetUserId);
    }

    // 응원 보내기
    @Transactional
    public void sendCheer(UUID senderId, UUID crewId, UUID receiverId, CheerRequest request) {
        // 같은 크루 멤버인지 확인
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, senderId)) {
            throw new BusinessException("크루 멤버가 아닙니다.", HttpStatus.FORBIDDEN);
        }

        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, receiverId)) {
            throw new BusinessException("대상 유저가 크루 멤버가 아닙니다.", HttpStatus.NOT_FOUND);
        }

        // 자기 자신에게 응원 불가
        if (senderId.equals(receiverId)) {
            throw new BusinessException("자신에게 응원을 보낼 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 응원 제한 체크 - 10분에 1회
        LocalDateTime limitTime = LocalDateTime.now()
                .minusMinutes(CHEER_LIMIT_MINUTES);
        long recentCheers = crewCheerRepository
                .countRecentCheers(senderId, receiverId, limitTime);

        if (recentCheers > 0) {
            throw new BusinessException("10분에 1회만 응원을 보낼 수 있습니다.", HttpStatus.TOO_MANY_REQUESTS);
        }
        User sender = findActiveUser(senderId);
        User receiver = findActiveUser(receiverId);
        Crew crew = findCrew(crewId);

        CrewCheer cheer = CrewCheer.send(
                sender, receiver, crew, request.cheerType());
    }
    
    // 크루 삭제
    @Transactional
    public void deleteCrew(UUID userId, UUID crewId) {
        
        // 리더만 삭제 가능
        checkIsLeader(userId, crewId);
        
        Crew crew = findCrew(crewId);
        
        // 멤버가 본인(리더)만 있을 때만 삭제 가능
        long memberCount = crewMemberRepository.countByCrewId(crewId);
        if (memberCount > 1) {
            throw new BusinessException("멤버가 있는 크루는 삭제할 수 없습니다. 멤버를 모두 탈퇴시킨 후 삭제해주세요.",
                    HttpStatus.BAD_REQUEST);
        }

        // 리더 멤버 먼저 삭제 후 크루 삭제
        crewMemberRepository.findByCrewIdAndUserId(crewId, userId)
                .ifPresent(crewMemberRepository::delete);
        crewRepository.delete(crew);

        log.info("크루 삭제: crewId={}", crewId);
    }

    // ---- 공통 메서드 ----

    private Crew findCrew(UUID crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new BusinessException("크루를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 리더 여부 확인
    private void checkIsLeader(UUID userId, UUID crewId) {
        CrewMember member = crewMemberRepository
                .findByCrewIdAndUserId(crewId, userId)
                .orElseThrow(() -> new BusinessException("크루 멤버가 아닙니다.", HttpStatus.FORBIDDEN));

        if (!member.isLeader()) {
            throw new BusinessException("리더만 가능한 작업입니다.", HttpStatus.FORBIDDEN);
        }
    }
}
