package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PinService {
    private final PinRepository pinRepository;

    public long count() {
        return pinRepository.count();
    }


    public Pin write(double latitude, double longitude, LocalDateTime time) {
        Pin pin = new Pin(latitude, longitude, time);
        return pinRepository.save(pin);
    }

    public Optional<Pin> findById(long id) {
        return pinRepository.findById(id);
    }

    public List<Pin> findAll() {
        return pinRepository.findAll();
    }

    public void deleteById(long id) {
        Pin pin = pinRepository.findById(id).get();
        pinRepository.delete(pin);
    }
}
