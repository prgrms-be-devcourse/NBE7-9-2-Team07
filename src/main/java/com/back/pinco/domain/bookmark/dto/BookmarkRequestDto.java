package com.back.pinco.domain.bookmark.dto;

public record BookmarkRequestDto(
        Long userId,
        Long pinId
) {}