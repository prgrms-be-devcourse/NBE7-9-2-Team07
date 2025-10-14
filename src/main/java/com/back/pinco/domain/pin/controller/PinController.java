package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pins")
public class PinController {

    @Autowired
    private PinService pinService;



    @GetMapping("")
    public RsData<List<Pin>> getRadiusPins(
            @NotNull
            @Min(0)
            @Max(20000)
            @RequestParam
            double radius,

            @NotNull
            @Min(-90)
            @Max(90)
            @RequestParam double latitude,

            @NotNull
            @Min(-180)
            @Max(180)
            @RequestParam double longitude
    ) {
        List<Pin> pins = pinService.findNearPins(radius,latitude,longitude);
        if(pins.isEmpty()){
            return new RsData<>(
                    "204",
            "조회된 값이 없습니다.",
            null
                    );
        }
        return new RsData<List<Pin>>(
                "200",
                "성공적으로 처리되었습니다",
                pins
        );

    }

    @GetMapping("/all")
    public RsData<List<Pin>> getAll() {
        List<Pin> pins = pinService.findAll();
        if(pins.isEmpty()){
            return new RsData<>(
                    "204",
                    "조회된 값이 없습니다.",
                    null
            );
        }
        return new RsData<List<Pin>>(
                "200",
                "성공적으로 처리되었습니다",
                pins
        );

    }



}
