package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import java.time.LocalDateTime;
import java.util.List;

import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.user.entity.User;
import jakarta.persistence.*;



public record PinDto(
        Long id,
        Double latitude,
        Double longitude,
        String content,
        Long userId,
        List<PinTag> pinTags,
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
                pin.getPinTags(),
                pin.getLikeCount(),
                pin.getIsPublic(),
                pin.getCreatedAt(),
                pin.getModifiedAt()
        );
    }
}
