package com.back.pinco.domain.pin.dto;

import com.back.pinco.domain.tag.entity.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public record PostPinReqbody(
        @NotNull
        @Min(-90)
        @Max(90)
        Double latitude,

        @NotNull
        @Min(-180)
        @Max(180)
        Double longitude,
        @NotBlank
        String content
        //List<Tag> tags 태그 부분 완성 후 요청 받고->태그 서비스단에 넘겨서->최종 태그 받아서 -> 수정 및 생성하면 될듯
){
}