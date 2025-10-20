package com.back.pinco.domain.likes.service;

import com.back.pinco.domain.likes.dto.LikesStatusDto;
import com.back.pinco.domain.likes.dto.PinLikedUserDto;
import com.back.pinco.domain.likes.dto.UserLikedPinsDto;
import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final PinService pinService;
    private final UserService userService;
    private final LikesRepository likesRepository;


    /** 특정 핀에 대한 좋아요 수 조회 */
    public int getLikesCount(Long pinId) {
        return (int) likesRepository.countByPin_IdAndLikedTrue(pinId);
    }


    /** 좋아요 있으면 상태변경, 없으면 신규 생성 */
    private Likes teoggleLike(Long pinId, Long userId) {
        Pin pin = pinService.findById(pinId);
        User user = userService.userInform(userId);

        // 받아온 객체에서 꺼내써야하는지, 그냥 전달 받은 값으로 사용해도 되는지?
        Likes likes = likesRepository.findByPinIdAndUserId(pin.getId(), user.getId())
                .map(Likes::toggleLike)
                .orElse(new Likes(user, pin));

        try {
            return likesRepository.save(likes);
        } catch (Exception e){  // TODO : DataAccessException?
            throw new ServiceException(ErrorCode.LIKES_CHANGE_FAILED);
        }
    }

    public createPinLikesResponse createPinLikes(Long pinId, Long userId) {
        Likes rt_like = teoggleLike(pinId, userId);
        return new createPinLikesResponse(rt_like.getLiked(), getLikesCount(pinId));
    }

    public deletePinLikesResponse togglePinLikes(Long pinId, Long userId) {
        Likes rt_like = teoggleLike(pinId, userId);
        return new deletePinLikesResponse(rt_like.getLiked(), getLikesCount(pinId));
    }


    /** 해당 핀을 좋아요 누른 유저 ID 목록 전달 */
    public List<PinLikedUserResponse> getUsersWhoLikedPin(Long pinId) {
        if (!pinService.checkId(pinId)) {
            throw new ServiceException(ErrorCode.LIKES_PIN_NOT_FOUND);
        }
        return likesRepository.findUsersByPinIdAndLikedTrue(pinId)
                .stream()
                .map(PinLikedUserResponse::formEntry)
                .toList();
    }

    /** 특정 사용자가 좋아요 누른 핀 목록 전달 */
    public List<PinsLikedByUserResponse> getPinsLikedByUser(Long userId) {
        userService.existsUserId(userId);
        return likesRepository.findPinsByUserIdAndLikedTrue(userId)
                .stream()
                .map(PinsLikedByUserResponse::formEntry)
                .toList();
    }

}
