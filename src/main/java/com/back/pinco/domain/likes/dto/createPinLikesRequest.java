package com.back.pinco.domain.likes.dto;

import jakarta.validation.constraints.NotNull;

public record createPinLikesRequest(
        @NotNull
        Long userId
) {
}
