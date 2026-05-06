package com.main.heatrun.domain.running.dto;

import com.main.heatrun.domain.entity.RunningSession;

import java.time.LocalDateTime;
import java.util.UUID;

public record RunningSessionResponse(

        UUID id,
        String status,
        String weatherCondition,
        Double totalDistanceKm,
        Integer durationSeconds,
        Double avgPace,
        Double caloriesBurned,
        UUID ghostSessionId,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static RunningSessionResponse from(RunningSession session) {
        return new RunningSessionResponse(
                session.getId(),
                session.getStatus().name(),
                session.getWeatherCondition() != null
                        ? session.getWeatherCondition().name() : null,
                session.getTotalDistanceKm(),
                session.getDurationSeconds(),
                session.getAvgPace(),
                session.getCaloriesBurned(),
                session.getGhostSession() != null
                        ? session.getGhostSession().getId() : null,
                session.getStartedAt(),
                session.getEndedAt()
        );
    }
}
