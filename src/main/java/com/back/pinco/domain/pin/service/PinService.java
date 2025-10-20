package com.back.pinco.domain.pin.service;

import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.CreatePinRequest;
import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.dto.UpdatePinContentRequest;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.service.PinTagService;
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
        Pin pin = pinRepository.findById(id).orElseThrow(() -> new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getDeleted()){
            throw new ServiceException(ErrorCode.PIN_NOT_FOUND);
        }
        return pin;
    }

    public Boolean checkId(long id) {
        return pinRepository.findById(id).isPresent();
    }

    public List<Pin> findAll() {
        List<Pin> pins = pinRepository.findAll().stream().filter(pin -> !pin.getDeleted()).toList();
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    public List<Pin> findNearPins(double latitude,double longitude) {
        List<Pin> pins = pinRepository.findPinsWithinRadius(latitude,longitude,1000.0);
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

}
