package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.global.geometry.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PinService {
    private final PinRepository pinRepository;
    private final GeometryUtil geometryUtil;

    public long count() {
        return pinRepository.count();
    }


    public Pin write(double latitude, double longitude) {
        Point point = geometryUtil.createPoint(latitude, longitude);
        Pin pin = new Pin(point);
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


    public List<Pin> findNearPins(double latitude,double longitude,double radius) {
        return pinRepository.findPinsWithinRadius(latitude,longitude,radius);
    }
}
