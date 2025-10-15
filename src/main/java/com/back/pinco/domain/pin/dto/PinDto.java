package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.global.geometry.GeometryUtil;
import java.time.LocalDateTime;

public record PinDto(
        Long id,
        Double latitude,
        Double longitude,
        LocalDateTime createAt
) {
    public PinDto(Pin pin, GeometryUtil geometryUtil) {
        this(
                pin.getId(),
                geometryUtil.getLatitude(pin.getPoint()),
                geometryUtil.getLongitude(pin.getPoint()),
                pin.getCreateAt()
        );
    }
}