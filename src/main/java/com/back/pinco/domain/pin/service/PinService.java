package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.pin.dto.CreatePinRequest;
import com.back.pinco.domain.pin.dto.UpdatePinContentRequest;
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


    public long count() {
        return pinRepository.count();
    }


    public Pin write(User actor, CreatePinRequest pinReqbody) {
        Point point = GeometryUtil.createPoint(pinReqbody.longitude(), pinReqbody.latitude());
        try {
            Pin pin = new Pin(point, actor, pinReqbody.content());
            return pinRepository.save(pin);
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_CREATE_FAILED);
        }
    }

    public Pin findById(long id) {
        // TODO: 인증 추가하여 공개/비공개 열람 범위 정하고, 삭제된 것도 필요한지 확인
        Pin pin = pinRepository.findByIdWithConditions(id,true,false);
        if(pin==null){
            throw new ServiceException(ErrorCode.PIN_NOT_FOUND);
        }
        return pin;
    }

    public Boolean checkId(long id) {
        return pinRepository.findById(id).isPresent();
    }

    public List<Pin> findAll() {
        List<Pin> pins = pinRepository.findByIsPublicAndDeleted(true, false);
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    public List<Pin> findNearPins(double latitude,double longitude) {
        // TODO: 인증 추가하여 공개/비공개 열람 범위 정하고, 삭제된 것도 필요한지 확인
        List<Pin> pins = pinRepository.findPinsWithinRadius(latitude,longitude,1000.0, true,false);
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    @Transactional
    public Pin update(User actor, Long pinId, UpdatePinContentRequest updatePinContentRequest) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        // TODO: 인증 추가
        try {
            pin.update(updatePinContentRequest);
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
        }

        return pin;
    }

    @Transactional
    public Pin changePublic(User actor, Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        // TODO: 인증 추가
        try {
        pin.togglePublic();
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
        }
        return pin;
    }

    public void deleteById(Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        // TODO: 인증 추가
        try {
            pin.setDeleted();
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_DELETE_FAILED);
        }
        pinRepository.save(pin);
    }

    /**
     * 핀 좋아요 수 갱신
     * @param pin
     * @param likecount
     * @return Pin
     */
    @Transactional
    public Pin updateLikes(Pin pin, int likecount) {
        pin.setLikeCount(likecount);
        return pinRepository.save(pin);
    }

    public List<Pin> findByUserId(User actor, User writer) {
        //TODO: 인증 추가해서 비공개-남이 작성한 건 걸러주기
        List<Pin> pins = pinRepository.findByUserAndIsPublicAndDeleted(writer, true, false);
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }
}
