package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    // 세션의 전체 GPS 좌표 - 순서대로 (고스트 재현 시 사용)
    List<RoutePoint> findBySessionIdOrderBySequenceNumberAsc(UUID sessionId);

    // 세션 좌표 수 - 러닝 유효성 검사
    long countBySessionId(UUID sessionId);

    // 세션 좌표 전체 삭제 - 러닝 취소 시
    void deleteBySessionId(UUID sessionId);
}
