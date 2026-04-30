package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.PedometerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PedometerLogRepository extends JpaRepository<PedometerLog, UUID> {

    // 특정 날짜 조회 - upsert 처리
    Optional<PedometerLog> findByUserIdAndLogDate(UUID userId, LocalDate logDate);

    // 연속 달성 여부 - 스트릭 계산
    @Query("""
           SELECT pl FROM PedometerLog pl
           WHERE pl.user.id = :userId
           AND pl.logDate BETWEEN :startDate AND :endDate
           AND pl.goalAchieved = true
           ORDER BY pl.logDate DESC
           """)
    List<PedometerLog> findAchievedLogs(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
