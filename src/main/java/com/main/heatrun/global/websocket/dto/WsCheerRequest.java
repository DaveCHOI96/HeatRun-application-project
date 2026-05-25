package com.main.heatrun.global.websocket.dto;

import com.main.heatrun.global.enums.CheerType;
import jakarta.validation.constraints.NotNull;

public record WsCheerRequest(

        @NotNull(message = "응원 유형은 필수입니다.")
        CheerType cheerType
) {
}
