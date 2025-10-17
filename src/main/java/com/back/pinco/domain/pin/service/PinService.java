package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.dto.PutPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import com.back.pinco.global.geometry.GeometryUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PinService {
    private final PinRepository pinRepository;
    private final GeometryUtil geometryUtil;

    public long count() {
        return pinRepository.count();
    }


    public Pin write(User actor, PostPinReqbody pinReqbody) {
        try {
            Point point = geometryUtil.createPoint(pinReqbody.longitude(), pinReqbody.latitude());
            Pin pin = new Pin(point, actor, pinReqbody.content());
            return pinRepository.save(pin);
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_CREATE_FAILED);
        }

    }

    public Pin findById(long id) {
        Pin pin = pinRepository.findById(id).orElseThrow(() -> new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getIsDeleted()){
            throw new ServiceException(ErrorCode.PIN_NOT_FOUND);
        }
        return pin;
    }

    public Boolean checkId(long id) {
        return pinRepository.findById(id).isPresent();
    }

    public List<Pin> findAll() {
        List<Pin> pins = pinRepository.findAll().stream().filter(pin -> !pin.getIsDeleted()).toList();
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    public List<Pin> findNearPins(double latitude,double longitude) {
        List<Pin> pins = pinRepository.findPinsWithinRadius(latitude,longitude,1000.0);
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    @Transactional
    public Pin update(User actor, Long pinId, PutPinReqbody putPinReqbody) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        //근데 이제 인증 들어가긴 해야함
        try {
            pin.update(putPinReqbody);
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
        }

        return pin;
    }

    @Transactional
    public Pin changePublic(User actor, Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        //여기에 이제 인증 들어가긴 해야함
        try {
        pin.togglePublic();
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
        }
        return pin;
    }

    public void deleteById(Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        //여기에 이제 인증 들어가긴 해야함
        try {
            pin.setIsDeleted();
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_DELETE_FAILED);
        }
        pinRepository.save(pin);
    }


}
