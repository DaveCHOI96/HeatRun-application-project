package com.main.heatrun.global.websocket.dto;

// 클라이언트에서 서버로 보내는 위치 요청
public record LocationRequest(
        Double latitude,
        Double longitude,
        Double speed,
        String status  // RUNNING / PAUSED / STOPPED
) {
}
