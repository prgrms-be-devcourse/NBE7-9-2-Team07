package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import java.time.LocalDateTime;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.user.entity.User;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;


public record PinDto(
        Long id,
        Point point,
        String content,
        User user,
        Tag tag,
        int likeCount,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public PinDto(Pin pin) {
        this(
                pin.getId(),
                pin.getPoint(),
                pin.getContent(),
                pin.getUser(),
                pin.getTag(),
                pin.getLikeCount(),
                pin.getIsPublic(),
                pin.getCreatedAt(),
                pin.getModifiedAt()
        );
    }
}
