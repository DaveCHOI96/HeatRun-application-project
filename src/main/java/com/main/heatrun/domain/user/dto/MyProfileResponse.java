package com.main.heatrun.domain.user.dto;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.global.enums.LocationShareScope;

import java.util.UUID;

public record MyProfileResponse(
        UUID id,
        String email,
        String nickname,
        String profileImageUrl,
        String provider,
        String role,
        String status,
        LocationShareScope locationShareScope,
        Integer privacyZoneRadius
) {
    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getProvider().name(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getLocationShareScope(),
                user.getPrivacyZoneRadius()
        );
    }
}
