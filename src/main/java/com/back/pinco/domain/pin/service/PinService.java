package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.dto.PinPostReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.geometry.GeometryUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    public Pin write(User actor, PinPostReqbody pinReqbody) {
        Point point = geometryUtil.createPoint(pinReqbody.longitude(), pinReqbody.latitude());
        Pin pin = new Pin(point, actor);
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
        pin.setIsDeleted();
        pinRepository.save(pin);
    }


    public List<Pin> findNearPins(double latitude,double longitude) {
        return pinRepository.findPinsWithinRadius(latitude,longitude,1000.0);
    }


}
