package com.main.heatrun.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
        String nickname
) {
}
