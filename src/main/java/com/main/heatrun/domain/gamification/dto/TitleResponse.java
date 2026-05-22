package com.main.heatrun.domain.gamification.dto;

import java.util.UUID;

public record TitleResponse(
        UUID id,
        String code,
        String name,
        String description,
        String conditionType,
        Integer conditionValue,
        String iconUrl,
        Boolean isLimited,
        Boolean isEarned, // 내가 획득했는지
        Boolean isEquipped // 장착 중인지
) {
}
