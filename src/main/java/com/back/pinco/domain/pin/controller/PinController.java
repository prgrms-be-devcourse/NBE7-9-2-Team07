package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.pin.service.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pin")
public class PinController {

    private final PinService pinService;

    @GetMapping()
    public String getPin() {
        return "";
    }

    @PostMapping()
    public String addPin() {
        return "";
    }

    @DeleteMapping()
    public String deletePin() {
        return "";
    }
    @PutMapping()
    public String updatePin() {
        return "";
    }
}
