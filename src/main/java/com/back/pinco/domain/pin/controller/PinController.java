package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.global.geometry.GeometryUtil;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pins")
public class PinController {

    @Autowired
    private PinService pinService;


    @GetMapping("")
    public RsData<List<PinDto>> getRadiusPins(
            @NotNull
            @Min(-90)
            @Max(90)
            @RequestParam double latitude,

            @NotNull
            @Min(-180)
            @Max(180)
            @RequestParam double longitude
    ) {
        List<Pin> pins = pinService.findNearPins(latitude, longitude);

        List<PinDto> pinDtos = pins.stream()
                .map(PinDto::new)
                .collect(Collectors.toList());

        if (pinDtos.isEmpty()) {
            return new RsData<>(
                    "204",
                    "조회된 값이 없습니다.",
                    null
            );
        }
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDtos
        );
    }

    @GetMapping("/all")
    public RsData<List<PinDto>> getAll() {
        List<Pin> pins = pinService.findAll();

        List<PinDto> pinDtos = pins.stream()
                .map(PinDto::new)
                .toList();

        if (pins.isEmpty()) {
            return new RsData<>(
                    "204",
                    "조회된 값이 없습니다.",
                    null
            );
        }
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDtos
        );
    }
}