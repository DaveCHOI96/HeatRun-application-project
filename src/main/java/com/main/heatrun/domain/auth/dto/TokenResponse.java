package com.main.heatrun.domain.auth.dto;

import java.util.UUID;

public record TokenResponse(

        UUID userId,
        String accessToken,
        String refreshToken,
        String nickname,
        String email
) {
}
