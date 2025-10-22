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
        if(actor==null) throw new ServiceException(ErrorCode.PIN_NO_PERMISSION);
        Point point = GeometryUtil.createPoint(pinReqbody.longitude(), pinReqbody.latitude());
        try {
            Pin pin = new Pin(point, actor, pinReqbody.content());
            return pinRepository.save(pin);
        }catch(Exception e){
            throw new ServiceException(ErrorCode.PIN_CREATE_FAILED);
        }
    }

    public Pin findById(long id, User actor) {
        if(actor==null){
            return pinRepository.findPublicPinById(id)
                    .orElseThrow(() -> new ServiceException(ErrorCode.PIN_NOT_FOUND));
        }

        return pinRepository.findAccessiblePinById(id, actor.getId())
                .orElseThrow(() -> new ServiceException(ErrorCode.PIN_NOT_FOUND));
    }

    public Boolean checkId(long id) {
        return pinRepository.findById(id).isPresent();
    }

    public List<Pin> findAll(User actor) {
        List<Pin> pins;
        if(actor==null){
            pins= pinRepository.findAllPublicPins();
        }else {
            pins = pinRepository.findAllAccessiblePins(actor.getId());
        }
        if(pins.isEmpty()){ throw new ServiceException(ErrorCode.PINS_NOT_FOUND);}

        return pins;
    }

    public List<Pin> findNearPins(double latitude,double longitude, User actor) {
        List<Pin> pins;
        if(actor==null){
            pins=  pinRepository.findPublicPinsWithinRadius(latitude,longitude,1000.0);
        }else {
            pins =  pinRepository.findPinsWithinRadius(latitude,longitude,1000.0, actor.getId());
        }
        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    public List<Pin> findByUserId(User actor, User writer) {
        List<Pin> pins;
        if(actor==null){
            pins= pinRepository.findPublicByUser(writer.getId());
        }else {
            pins = pinRepository.findAccessibleByUser(writer.getId(), actor.getId());
        }

        if(pins.isEmpty()) throw new ServiceException(ErrorCode.PINS_NOT_FOUND);
        return pins;
    }

    @Transactional
    public Pin update(User actor, Long pinId, UpdatePinContentRequest updatePinContentRequest) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getUser().getId().equals(actor.getId())){
            try {
                pin.update(updatePinContentRequest);
            }catch(Exception e){
                throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
            }
        }else{
            throw new ServiceException(ErrorCode.PIN_NO_PERMISSION);
        }


        return pin;
    }

    @Transactional
    public Pin changePublic(User actor, Long pinId) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getUser().getId().equals(actor.getId())){
            try {
                pin.togglePublic();
            }catch(Exception e){
                throw new ServiceException(ErrorCode.PIN_UPDATE_FAILED);
            }
        }else{
            throw new ServiceException(ErrorCode.PIN_NO_PERMISSION);
        }

        return pin;
    }

    public void deleteById(Long pinId, User actor) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getUser().getId().equals(actor.getId())){
            try {
                pin.setDeleted();
            }catch(Exception e){
                throw new ServiceException(ErrorCode.PIN_DELETE_FAILED);
            }
        }else{
            throw new ServiceException(ErrorCode.PIN_NO_PERMISSION);
        }

        pinRepository.save(pin);
    }

    public void unDeleteById(Long pinId, User actor) {
        Pin pin = pinRepository.findById(pinId).orElseThrow(()->new ServiceException(ErrorCode.PIN_NOT_FOUND));
        if(pin.getUser().getId().equals(actor.getId())){
            try {
                pin.unSetDeleted();
            }catch(Exception e){
                throw new ServiceException(ErrorCode.PIN_DELETE_FAILED);
            }
        }else{
            throw new ServiceException(ErrorCode.PIN_NO_PERMISSION);
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
