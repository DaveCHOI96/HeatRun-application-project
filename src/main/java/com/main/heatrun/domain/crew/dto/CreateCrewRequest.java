package com.main.heatrun.domain.crew.dto;

import com.main.heatrun.global.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCrewRequest(

        @NotBlank(message = "크루 이름은 필수입니다.")
        @Size(min = 2, max = 20, message = "크루 이름은 2~20자 사이여야 합니다.")
        String name,

        @Size(max = 200, message = "크루 소개는 200자 이하여야 합니다.")
        String description,

        @NotBlank(message = "공개 여부는 필수입니다.")
        Visibility visibility
) {
}
