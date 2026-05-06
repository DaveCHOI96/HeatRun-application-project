package com.main.heatrun.domain.running.dto;


import jakarta.validation.constraints.NotNull;

public record RoutePointRequest(

        @NotNull(message = "위도는 필수입니다.")
        Double latitude,

        @NotNull(message = "경도는 필수입니다.")
        Double longitude,

        Double altitude,
        Double speed,

        @NotNull(message = "순서 번호는 필수입니다.")
        Integer sequenceNumber
) {
}
