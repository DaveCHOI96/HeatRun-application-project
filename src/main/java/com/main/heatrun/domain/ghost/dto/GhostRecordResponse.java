package com.main.heatrun.domain.ghost.dto;

import com.main.heatrun.domain.entity.GhostRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public record GhostRecordResponse(
        UUID id,
        UUID originalSessionId,
        String recordType,
        Boolean isPublic,
        Double totalDistanceKm,
        Integer durationSeconds,
        String ownerNickname,
        LocalDateTime createdAt
) {
    public static GhostRecordResponse from(GhostRecord record) {
        return new GhostRecordResponse(
                record.getId(),
                record.getOriginalSession().getId(),
                record.getRecordType().name(),
                record.getIsPublic(),
                record.getTotalDistanceKm(),
                record.getDurationSeconds(),
                record.getOwnerUser().getNickname(),
                record.getCreatedAt()
        );
    }
}
