package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import java.time.LocalDateTime;

public record PinDto(
        Long id,
        Double latitude,
        Double longitude,
        LocalDateTime createAt
) {
    public PinDto(Pin pin) {
        this(
                pin.getId(),
                pin.getPoint().getY(), // latitude
                pin.getPoint().getX(), // longitude
                pin.getCreateAt()
        );
    }
}
