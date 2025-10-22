package com.back.pinco.domain.likes.service;

import com.back.pinco.domain.likes.dto.PinLikedUserResponse;
import com.back.pinco.domain.likes.dto.PinsLikedByUserResponse;
import com.back.pinco.domain.likes.dto.createPinLikesResponse;
import com.back.pinco.domain.likes.dto.deletePinLikesResponse;
import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikesService {

    private final PinService pinService;
    private final UserService userService;
    private final LikesRepository likesRepository;


    /** 특정 핀에 대한 좋아요 수 조회 */
    @Transactional(readOnly = true)
    public int getLikesCount(Long pinId) {
        return (int) likesRepository.countByPin_IdAndLikedTrue(pinId);
    }


    /** 좋아요 있으면 상태변경, 없으면 신규 생성 */
    private Likes toggleLike(Long pinId, Long userId, boolean isLiked) {
        User user = userService.findById(userId);
        Pin pin = pinService.findById(pinId, user);

        int likeCnt = pin.getLikeCount();

        try {
            Likes getLikes = likesRepository.findByPinIdAndUserId(pin.getId(), user.getId())
                    .map(like -> like.toggleLike(isLiked))
                    .orElse(new Likes(user, pin));

            Likes rt_like = likesRepository.save(getLikes);

            // 핀 테이블에 업데이트
            likeCnt = isLiked ? likeCnt + 1
                    : likeCnt == 0 ? 0 : likeCnt - 1;

            pinService.updateLikes(pin, likeCnt);

            return rt_like;
        } catch (Exception e){  // TODO : DataAccessException?
            throw new ServiceException(ErrorCode.LIKES_CHANGE_FAILED);
        }
    }

    public createPinLikesResponse createPinLikes(Long pinId, Long userId) {
        Likes rt_like = toggleLike(pinId, userId, true);

        return new createPinLikesResponse(rt_like.getLiked(), getLikesCount(pinId));
    }

    public deletePinLikesResponse togglePinLikes(Long pinId, Long userId) {
        Likes rt_like = toggleLike(pinId, userId, false);
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
