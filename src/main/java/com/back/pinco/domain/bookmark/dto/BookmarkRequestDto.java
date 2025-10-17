package com.back.pinco.domain.bookmark.dto;

/**
 * 북마크 생성 요청 DTO
 * @param userId 사용자 ID
 * @param pinId 핀 ID
 */
public record BookmarkRequestDto(
        Long userId,
        Long pinId
) {}