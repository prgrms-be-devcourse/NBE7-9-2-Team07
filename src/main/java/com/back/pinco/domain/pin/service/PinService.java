package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.dto.PutPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.geometry.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PinService {
    private final PinRepository pinRepository;
    private final GeometryUtil geometryUtil;

    public long count() {
        return pinRepository.count();
    }

    public Pin write(User actor, PostPinReqbody pinReqbody) {
        Point point = geometryUtil.createPoint(pinReqbody.longitude(), pinReqbody.latitude());
        Pin pin = new Pin(point, actor, pinReqbody.content());
        return pinRepository.save(pin);
    }

    public Optional<Pin> findById(long id) {
        return pinRepository.findById(id);
    }

    public List<Pin> findAll() {
        return pinRepository.findAll();
    }

    public List<Pin> findNearPins(double latitude,double longitude) {
        return pinRepository.findPinsWithinRadius(latitude,longitude,1000.0);
    }

    public Pin update(User actor, Long pinId, PutPinReqbody putPinReqbody) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(NoSuchElementException::new);
        //여기에 이제 인증 들어가긴 해야함
        pin.update(putPinReqbody);
        return pin;
    }

    public Pin changePublic(User actor, Long pinId, PutPinReqbody putPinReqbody) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(NoSuchElementException::new);
        //여기에 이제 인증 들어가긴 해야함
        pin.togglePublic();
        return pin;
    }

    public void deleteById(Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(NoSuchElementException::new);
        //여기에 이제 인증 들어가긴 해야함
        pin.setIsDeleted();
        pinRepository.save(pin);
    }
}
