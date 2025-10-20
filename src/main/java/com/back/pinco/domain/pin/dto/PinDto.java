package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;


public record PinDto(
        Long id,
        Double latitude,
        Double longitude,
        String content,
        Long userId,
        List<Tag> pinTags,
        int likeCount,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PinDto(Pin pin) {
        this(
                pin.getId(),
                pin.getPoint().getY(),
                pin.getPoint().getX(),
                pin.getContent(),
                pin.getUser().getId(),
                null,
                pin.getLikeCount(),
                pin.getIsPublic(),
                pin.getCreatedAt(),
                pin.getModifiedAt()
        );
    }

}
