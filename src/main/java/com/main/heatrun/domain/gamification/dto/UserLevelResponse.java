package com.main.heatrun.domain.gamification.dto;

import com.main.heatrun.domain.entity.UserLevel;

public record UserLevelResponse(
        Integer currentLevel,
        Integer totalExp,
        Integer expToNextLevel,
        Integer progressPercent // 현재 레벨 진행률 %
) {
    public static UserLevelResponse from(UserLevel level) {
        int progress = (int) ((double) level.getTotalExp() / level.getExpToNextLevel() * 100);
        return new UserLevelResponse(
                level.getCurrentLevel(),
                level.getTotalExp(),
                level.getExpToNextLevel(),
                progress
        );
    }
}
