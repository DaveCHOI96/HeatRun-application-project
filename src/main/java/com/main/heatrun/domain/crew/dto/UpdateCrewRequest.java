package com.main.heatrun.domain.crew.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCrewRequest(

        @NotBlank(message = "크루 이름은 필수입니다.")
        @Size(min = 2, max = 50, message = "크루 이름은 2~50자 사이여야 합니다.")
        String name,

        @Size(max = 200, message = "크루 소개는 200자 이하여야 합니다.")
        String description
) {
}
