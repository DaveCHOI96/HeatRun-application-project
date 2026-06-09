package com.main.heatrun.global.websocket.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// 클라이언트에서 서버로 보내는 위치 요청
public record LocationRequest(
        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        Double longitude,

        Double speed,

        // 허용값 제한
        @NotNull(message = "상태는 필수입니다.")
        @Pattern(regexp = "RUNNING|PAUSED|STOPPED", message = "유효하지 않은 상태값입니다.")
        String status  // RUNNING / PAUSED / STOPPED
) {
}
