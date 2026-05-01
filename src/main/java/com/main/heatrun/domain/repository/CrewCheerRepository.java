package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.CrewCheer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CrewCheerRepository extends JpaRepository<CrewCheer, Long> {

    // 특정 시간 이후 응원 횟수 - 남용 방지(10분에 1회 제한)
    @Query("""
           SELECT COUNT(cc) FROM CrewCheer cc
           WHERE cc.sender.id = :senderId
           AND cc.receiver.id = :receiverId
           AND cc.sentAt >= :after
           """)
    long countRecentCheers(
            @Param("senderId") UUID senderId,
            @Param("receiverId") UUID receiverId,
            @Param("after")LocalDateTime after);
}
