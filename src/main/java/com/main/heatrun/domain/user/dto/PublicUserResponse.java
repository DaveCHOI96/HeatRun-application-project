package com.main.heatrun.domain.user.dto;

import com.main.heatrun.domain.entity.User;

import java.util.UUID;

public record PublicUserResponse(
        UUID id,
        String nickname,
        String profileImageUrl
) {
    public static PublicUserResponse from(User user) {
        return new PublicUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl()
        );
    }
}
