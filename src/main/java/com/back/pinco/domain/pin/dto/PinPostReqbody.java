package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.tag.entity.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

public record PinPostReqbody(
        @NotNull
        @Min(-90)
        @Max(90)
        @RequestParam double latitude,

        @NotNull
        @Min(-180)
        @Max(180)
        @RequestParam double longitude,
        @NotBlank
        String content
){
}