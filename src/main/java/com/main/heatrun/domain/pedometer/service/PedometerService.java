package com.main.heatrun.domain.pedometer.service;

import com.main.heatrun.domain.entity.PedometerLog;
import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.pedometer.dto.PedometerLogResponse;
import com.main.heatrun.domain.pedometer.dto.UpdateStepRequest;
import com.main.heatrun.domain.repository.PedometerLogRepository;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedometerService {

    private final PedometerLogRepository pedometerLogRepository;
    private final UserRepository userRepository;

    // 오늘 만보기 조회 (없으면 자동 생성)
    @Transactional
    public PedometerLogResponse getToday(UUID userId) {
        User user = findUser(userId);
        LocalDate today = LocalDate.now();

        PedometerLog log = pedometerLogRepository
                .findByUserIdAndLogDate(userId, today)
                .orElseGet(() -> pedometerLogRepository.save(
                        PedometerLog.create(user, today, 10000)
                ));
        return PedometerLogResponse.from(log);
    }

    // 만보기 업데이트
    @Transactional
    public PedometerLogResponse updateToday(UUID userId, UpdateStepRequest request) {
        User user = findUser(userId);
        LocalDate today = LocalDate.now();

        PedometerLog log = pedometerLogRepository
                .findByUserIdAndLogDate(userId, today)
                .orElseGet(() -> pedometerLogRepository.save(
                        PedometerLog.create(user, today, 10000)
                ));
        log.updateStepCount(request.stepCount());
        return PedometerLogResponse.from(log);
    }

    // 최근 7일 기록
    @Transactional(readOnly = true)
    public List<PedometerLogResponse> getWeekly(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        return pedometerLogRepository
                .findByUserIdAndLogDateBetweenOrderByLogDateDesc(userId, weekAgo, today)
                .stream()
                .map(PedometerLogResponse::from)
                .collect(Collectors.toList());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
