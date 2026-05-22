package com.main.heatrun.domain.gamification.dto;

import com.main.heatrun.domain.entity.UserTitle;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserTitleResponse(
        UUID titleId,
        String code,
        String name,
        String iconUrl,
        Boolean isEquipped,
        LocalDateTime earnedAt
) {
    public static UserTitleResponse from(UserTitle userTitle) {
        return new UserTitleResponse(
                userTitle.getTitle().getId(),
                userTitle.getTitle().getCode(),
                userTitle.getTitle().getName(),
                userTitle.getTitle().getIconUrl(),
                userTitle.getIsEquipped(),
                userTitle.getEarnedAt()
        );
    }
}
