package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.service.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pin")
public class PinController {

    private final PinService pinService;


}
