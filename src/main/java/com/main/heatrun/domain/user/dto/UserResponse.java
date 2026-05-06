package com.main.heatrun.domain.user.dto;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.global.enums.LocationShareScope;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String nickname,
        String profileImageUrl,
        String provider,
        String status,
        LocationShareScope locationShareScope,
        Integer privacyZoneRadius
) {
    // Entity -> DTO 변환
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getProvider().name(),
                user.getStatus().name(),
                user.getLocationShareScope(),
                user.getPrivacyZoneRadius()
        );
    }
}
