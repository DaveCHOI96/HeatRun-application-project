package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.GhostRecord;
import com.main.heatrun.global.enums.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GhostRecordRepository extends JpaRepository<GhostRecord, UUID> {

    // 유저의 PB 고스트 조회
    Optional<GhostRecord> findByOwnerUserIdAndRecordType(
            UUID userId, RecordType recordType);

    // 공개된 고스트 목록 - 다른 유저가 선택 가능
    List<GhostRecord> findByIsPublicTrueOrderByDurationSecondsAsc();

    // 팔로우 중인 유저들의 공개 고스트
    @Query("""
           SELECT gr FROM GhostRecord gr
           WHERE gr.ownerUser.id IN :userIds
           AND gr.isPublic = true
           ORDER BY gr.durationSeconds ASC
           """)
    List<GhostRecord> findPublicGhostsByUsers(@Param("userIds") List<UUID> userIds);
}
