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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final PinService pinService;
    private final UserService userService;
    private final LikesRepository likesRepository;


    /**
     * 특정 핀에 대한 좋아요 수 조회
     *
     * @param pinId 핀 ID
     * @return 좋아요 수
     */
    public int getLikesCount(Long pinId) {
        if (!likesRepository.existsByPin_Id(pinId)) {
            return 0;
        }

        return (int) likesRepository.countByPin_Id(pinId);
    }


    /**
     * 특정 (핀, 사용자)에 대한 좋아요 존재 여부 확인
     *
     * @param pinId  핀 ID
     * @param userId 사용자 ID
     * @return 존재하면 true, 없으면 false
     */
    private boolean existByLikes(Long pinId, Long userId) {
        return likesRepository.existsByPin_IdAndUser_Id(pinId, userId);
    }


    /**
     * 좋아요 없으면 생성, 있으면 상태 변경
     *
     * @param pinId  좋아요를 토글할 핀 ID
     * @param userId 좋아요를 토글할 사용자 ID
     * @return 토글 후의 좋아요 상태와 총 좋아요 수 DTO
     */
    public createPinLikesResponse createPinLikes(Long pinId, Long userId) {

        Pin pin = pinService.findById(pinId);
        User user = userService.userInform(userId);

        return likesRepository.findByPinIdAndUserId(pin.getId(), user.getId())
                .map(likes -> {
                    likesRepository.save(likes.toggleLike());
                    return new createPinLikesResponse(likes.getLiked(), getLikesCount(pin.getId()));
                })
                .orElseGet(() -> {
                    likesRepository.save(new Likes(user, pin));
                    return new createPinLikesResponse(true, getLikesCount(pin.getId()));
                });
    }

    /**
     * 좋아요 상태 변경
     *
     * @param pinId
     * @param userId
     * @return 토글 후의 좋아요 상태와 총 좋아요 수 DTO
     */
    public deletePinLikesResponse deletePinLikes(Long pinId, Long userId) {

        Pin pin = pinService.findById(pinId);
        User user = userService.userInform(userId);

        return likesRepository.findByPinIdAndUserId(pin.getId(), user.getId())
                .map(likes -> {
                    likesRepository.save(likes.toggleLike());
                    return new deletePinLikesResponse(likes.getLiked(), getLikesCount(pin.getId()));
                })
                .orElseGet(() -> {
                    likesRepository.save(new Likes(user, pin));
                    return new deletePinLikesResponse(true, getLikesCount(pin.getId()));
                });
    }


    /**
     * 해당 핀을 좋아요 누른 유저 ID 목록 전달
     *
     * @param pinId 핀 ID
     * @return 유저 ID 목록
     */
    public List<PinLikedUserResponse> getUsersWhoLikedPin(Long pinId) {
        if (!pinService.checkId(pinId)) {
            throw new ServiceException(ErrorCode.LIKES_PIN_NOT_FOUND);
        }
        return likesRepository.findUsersByPinId(pinId)
                .stream()
//                .distinct() // 구조상 중복될 일이 없는데 사용하는 편이 좋은가?
                .map(PinLikedUserResponse::formEntry)
                .toList();
    }

    /**
     * 특정 사용자가 좋아요 누른 핀 목록 전달
     *
     * @param userId 사용자 ID
     * @return 핀 목록
     */
    public List<PinsLikedByUserResponse> getPinsLikedByUser(Long userId) {
        // userId로 사용자 확인
//        if (!userService.checkExist(userId)) throw new ServiceException(ErrorCode.LIKES_USER_NOT_FOUND);
        return likesRepository.findPinsByUserId(userId)
                .stream()
//                .distinct()
                .map(PinsLikedByUserResponse::formEntry)
                .toList();
    }

}
