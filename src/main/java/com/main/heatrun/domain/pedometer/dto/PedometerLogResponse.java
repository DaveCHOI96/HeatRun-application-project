package com.main.heatrun.domain.pedometer.dto;

import com.main.heatrun.domain.entity.PedometerLog;

import java.time.LocalDate;
import java.util.UUID;

public record PedometerLogResponse(
        UUID id,
        LocalDate logDate,
        Integer stepCount,
        Integer dailyGoal,
        Boolean goalAchieved,
        Double distanceKm,
        Integer progressPercent
) {
    public static PedometerLogResponse from(PedometerLog log) {
        int progress = (int) ((double) log.getStepCount() / log.getDailyGoal() * 100);
        return new PedometerLogResponse(
                log.getId(),
                log.getLogDate(),
                log.getStepCount(),
                log.getDailyGoal(),
                log.getGoalAchieved(),
                log.getDistanceKm(),
                Math.min(progress, 100)
        );
    }
}
