package com.main.heatrun.domain.crew.dto;

import com.main.heatrun.global.enums.CheerType;
import jakarta.validation.constraints.NotNull;

public record CheerRequest(

        @NotNull(message = "응원 유형은 필수입니다.")
        CheerType cheerType
) {
}
