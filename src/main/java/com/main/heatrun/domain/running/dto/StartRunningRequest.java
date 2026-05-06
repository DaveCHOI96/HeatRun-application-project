package com.main.heatrun.domain.running.dto;

import com.main.heatrun.global.enums.WeatherCondition;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartRunningRequest(

        @NotNull(message = "날씨 정보는 필수입니다.")
        WeatherCondition weatherCondition,

        UUID ghostSessionId
) {
}
