package com.main.heatrun.domain.user.dto;


import com.main.heatrun.global.enums.LocationShareScope;
import jakarta.validation.constraints.NotBlank;

public record UpdateLocationScopeRequest(

        @NotBlank(message = "위치 공유 범위는 필수입니다.")
        LocationShareScope locationShareScope
) {
}
