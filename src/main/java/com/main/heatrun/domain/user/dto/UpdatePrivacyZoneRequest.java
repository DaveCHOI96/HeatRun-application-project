package com.main.heatrun.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdatePrivacyZoneRequest(

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        @NotNull(message = "반경은 필수입니다.")
        @Min(value = 100, message = "반경은 최소 100m 입니다.")
        @Max(value = 2000, message = "반경은 최대 2000m 입니다.")
        Integer radius
) {
}
