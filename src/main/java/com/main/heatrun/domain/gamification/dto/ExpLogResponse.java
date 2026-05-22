package com.main.heatrun.domain.gamification.dto;

import com.main.heatrun.domain.entity.ExpLog;

import java.time.LocalDateTime;

public record ExpLogResponse(
        Integer expEarned,
        String sourceType,
        LocalDateTime createdAt
) {
    public static ExpLogResponse from(ExpLog log) {
        return new ExpLogResponse(
                log.getExpEarned(),
                log.getSourceType().name(),
                log.getCreatedAt()
        );
    }
}
