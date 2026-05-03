package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.RunningSession;
import com.main.heatrun.global.enums.RunningStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RunningSessionRepository extends JpaRepository<RunningSession, UUID> {

    // 유저의 러닝 세션 목록 -> 최신순 (히트맵 이력)
    List<RunningSession> findByUserIdOrderByStartedAtDesc(UUID userId);

    // 유저의 진행중인 세션 - 중복 러닝 방지
    Optional<RunningSession> findByUserIdAndStatus(
            UUID userId, RunningStatus status);

    // 유저의 완료된 세션 중 최단 페이스 - PB 계산
    @Query("""
            SELECT rs FROM RunningSession rs
            WHERE rs.user.id = :userId
            AND rs.status = 'COMPLETED'
            AND rs.totalDistanceKm >= :minDistance
            ORDER BY rs.avgPace ASC
            LIMIT 1
            """)
    Optional<RunningSession> findPersonalBest(
            @Param("userId") UUID userId,
            @Param("minDistance") Double minDistance);

    // 유저의 완료된 세션 수 - 스트릭 계산용
    long countByUserIdAndStatus(UUID userId, RunningStatus status);
}
