package com.main.heatrun.domain.running.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompleteRunningRequest(

        @NotNull(message = "총 거리는 필수입니다.")
        @Positive(message = "총 거리는 0보다 커야 합니다.")
        Double totalDistanceKm,

        @NotNull(message = "총 시간은 필수입니다.")
        @Positive(message = "총 시간은 0보다 커야 합니다.")
        Integer durationSeconds,

        @NotNull(message = "평균 페이스는 필수입니다.")
        @Positive(message = "평균 페이스는 0보다 커야 합니다.")
        Double avgPace,

        @NotNull(message = "소모 칼로리는 필수입니다.")
        Double caloriesBurned
) {
}
