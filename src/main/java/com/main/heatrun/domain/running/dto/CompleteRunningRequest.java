package com.main.heatrun.domain.running.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

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

        // PositiveOrZero = 0은 가능 but 음수 불가능
        @NotNull(message = "소모 칼로리는 필수입니다.")
        @PositiveOrZero(message = "소모 칼로리는 0 이상이어야 합니다.")
        Double caloriesBurned
) {
}
