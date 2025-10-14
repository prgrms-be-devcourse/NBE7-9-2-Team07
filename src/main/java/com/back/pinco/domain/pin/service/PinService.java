package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PinService {
    private final PinRepository pinRepository;
}
