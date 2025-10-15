package com.back.pinco.domain.pin.dto;

import java.util.List;

public record PinListResponseDto(
        List<PinDto> pins
) { }