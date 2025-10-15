package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import java.time.LocalDateTime;

public record PinDto(
        Long id,
        Double longitude,
        Double latitude,
        LocalDateTime createAt
) {
    public PinDto(Pin pin) {
        this(
                pin.getId(),
                pin.getPoint().getX(), // longitude
                pin.getPoint().getY(), // latitude
                pin.getCreateAt()
        );
    }
}
