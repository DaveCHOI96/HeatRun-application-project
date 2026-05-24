package com.main.heatrun.domain.running.dto;

import com.main.heatrun.domain.entity.RoutePoint;

import java.time.LocalDateTime;

public record RoutePointResponse(
        Double latitude,
        Double longitude,
        Double altitude,
        Double speed,
        Integer sequenceNumber,
        LocalDateTime recordedAt
) {
    public static RoutePointResponse from(RoutePoint point) {
        return new RoutePointResponse(
                point.getLocation().getY(),
                point.getLocation().getX(),
                point.getAltitude(),
                point.getSpeed(),
                point.getSequenceNumber(),
                point.getRecordedAt()
        );
    }
}
