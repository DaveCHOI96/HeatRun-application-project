package com.main.heatrun.global.websocket.dto;

import com.main.heatrun.global.enums.CheerType;

import java.time.LocalDateTime;
import java.util.UUID;

// 실시간 응원 메시지
public record CheerMessage(
        UUID senderId,
        String senderNickname,
        UUID receiverId,
        CheerType cheerType,
        LocalDateTime timestamp
) {
}
