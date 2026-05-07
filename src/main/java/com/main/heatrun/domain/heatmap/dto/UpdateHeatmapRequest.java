package com.main.heatrun.domain.heatmap.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateHeatmapRequest(

        @NotNull(message = "GPS 좌표 목록은 필수입니다.")
        List<GpsCoordinate> coordinates,

        @NotNull(message = "줌 레벨은 필수입니다.")
        Integer zoomLevel,

        // true: 러닝 / false: 보행(만보기)
        @NotNull(message = "러닝 여부는 필수입니다.")
        Boolean isRunning
) {

    // 좌표 내부 record
    public record GpsCoordinate(
            @NotNull Double latitude,
            @NotNull Double longitude
    ) {}
}
