package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.dto.PinPostReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.geometry.GeometryUtil;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pins")
public class PinController {

    @Autowired
    private PinService pinService;

    //생성
    @PostMapping
    public RsData<PinDto> createPin(@RequestBody PinPostReqbody pinReqbody) {
        //User actor = rq.getActor(); jwt 구현 후 변경 예정. 일단 null 넣음
        User actor = null;
        Pin pin = pinService.write(actor, pinReqbody);
        PinDto pinDto= new PinDto(pin);
        return new RsData<>(
                "200",
        "작성 성공",
                pinDto
        );
    }





}