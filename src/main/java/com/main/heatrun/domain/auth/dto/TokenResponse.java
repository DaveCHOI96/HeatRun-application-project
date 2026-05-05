package com.main.heatrun.domain.auth.dto;

public record TokenResponse(

        String accessToken,
        String refreshToken,
        String nickname,
        String email
) {
}
