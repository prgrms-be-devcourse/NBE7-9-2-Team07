package com.back.pinco.domain.bookmark.dto;

import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.pin.dto.PinDto;

import java.time.LocalDateTime;

public record BookmarkDto(
        Long id,
        PinDto pin,
        LocalDateTime createdAt
) {
    public BookmarkDto(Bookmark bookmark) {
        this(
                bookmark.getId(),
                new PinDto(bookmark.getPin()),
                bookmark.getCreatedAt()
        );
    }
}

