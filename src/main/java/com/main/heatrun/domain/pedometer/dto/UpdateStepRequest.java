package com.main.heatrun.domain.pedometer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStepRequest(

        @NotNull(message = "걸음 수는 필수입니다.")
        @PositiveOrZero(message = "걸음 수는 0 이상이어야 합니다.")
        Integer stepCount
) {
}
