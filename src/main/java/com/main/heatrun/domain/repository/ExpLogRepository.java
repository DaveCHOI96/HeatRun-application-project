package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.ExpLog;
import com.main.heatrun.global.enums.ExpSourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExpLogRepository extends JpaRepository<ExpLog, Long> {

    // 유저의 최근 경험치 이력
    List<ExpLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    // 유저 최근 경험치 이력 (페이지 네이션 버전)
    Page<ExpLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // 출처별 경험치 합계 통계용
    @Query("""
           SELECT SUM(el.expEarned) FROM ExpLog el
           WHERE el.user.id = :userId
           AND el.sourceType = :sourceType
           """)
    Integer sumExpBySourceType(
            @Param("userId") UUID userId,
            @Param("sourceType") ExpSourceType sourceType);

}
