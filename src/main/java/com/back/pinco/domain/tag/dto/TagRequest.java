package com.back.pinco.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank(message = "태그 키워드를 입력해주세요.")
        String keyword
) {
}
