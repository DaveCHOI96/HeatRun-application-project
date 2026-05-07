package com.main.heatrun.global.websocket.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// 실기간 위치 메시지
public record LocationMessage(
        UUID userId,
        String nickname,
        String profileImageUrl,
        Double latitude,
        Double longitude,
        Double speed,
        String status,  // RUNNING / PAUSED / STOPPED
        LocalDateTime timestamp
) {
}
