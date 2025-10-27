package com.back.pinco.domain.likes.service;

import com.back.pinco.domain.likes.dto.PinLikedUserResponse;
import com.back.pinco.domain.likes.dto.PinLikesResponse;
import com.back.pinco.domain.likes.dto.PinsLikedByUserResponse;
import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikesService {

    private final LikesRepository likesRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;


    /**
     * 특정 핀에 대한 좋아요 수 조회
     */
    @Transactional(readOnly = true)
    public int getLikesCount(Long pinId) {
        return (int) likesRepository.countByPin_IdAndLikedTrue(pinId);
    }


    @Transactional
    public void pinUpdateLikes(Pin pin, boolean type) {
        int likeCnt = pin.getLikeCount();

        if (type) {
            pin.setLikeCount(likeCnt + 1);
        } else if (pin.getLikeCount() > 0) {
            pin.setLikeCount(likeCnt - 1);
        }

        pinRepository.save(pin);
    }

    @Transactional
    public PinLikesResponse changeLikes(Long pinId, Long userId, boolean isLiked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_INVALID_USER_INPUT));

        Pin pin = pinRepository.findAccessiblePinById(pinId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_INVALID_PIN_INPUT));

        Likes likes = likesRepository.save(toggleLikes(isLiked, pin, user));

        entityManager.flush();
        entityManager.clear();

        try {
            pinUpdateLikes(pin, isLiked);
            return new PinLikesResponse(likes.getLiked(), getLikesCount(pinId));
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.LIKES_UPDATE_PIN_FAILED);
        }
    }


    private Likes toggleLikes(boolean isLiked, Pin pin, User user) {
        try {
            return likesRepository.findByPinIdAndUserId(pin.getId(), user.getId())
                    .map(like -> like.toggleLike(isLiked))
                    .orElse(new Likes(user, pin));
        } catch (Exception e) {
            if (isLiked) {
                throw new ServiceException(ErrorCode.LIKES_CREATE_FAILED);
            } else {
                throw new ServiceException(ErrorCode.LIKES_REVOKE_FAILED);
            }
        }
    }


    /**
     * 해당 핀을 좋아요 누른 유저 ID 목록 전달
     */
    public List<PinLikedUserResponse> getUsersWhoLikedPin(Long pinId) {
        if (!pinRepository.existsById(pinId)) {
            throw new ServiceException(ErrorCode.LIKES_INVALID_PIN_INPUT);
        }

        return likesRepository.findUsersByPinIdAndLikedTrue(pinId)
                .stream()
                .map(PinLikedUserResponse::formEntry)
                .toList();
    }

    /**
     * 특정 사용자가 좋아요 누른 핀 목록 전달
     */
    public List<PinsLikedByUserResponse> getPinsLikedByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ServiceException(ErrorCode.LIKES_INVALID_USER_INPUT);
        }

        return likesRepository.findPinsByUserIdAndLikedTrue(userId)
                .stream()
                .filter(pin -> pin.getUser().getId().equals(userId) || pin.getIsPublic())
                .map(PinsLikedByUserResponse::formEntry)
                .toList();
    }

    /**탈퇴한 사용자의 좋아요 취소 */
    @Transactional
    public void updateDeleteUserLikedFalse(Long userId) {
        // 핀 조회
        List<Pin> likedPins = likesRepository.findPinsByUserIdAndLikedTrue(userId);

        if (likedPins.isEmpty()) return;

        try {
            likesRepository.updateLikedByUserId(userId);

            List<Pin> updatedPins = likedPins
                    .stream()
                    .map(pin -> updatePinLikeCount(pin))
                    .toList();

            pinRepository.saveAll(updatedPins);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.LIKES_UPDATE_PIN_FAILED);
        }
    }

    @Transactional
    public Pin updatePinLikeCount(Pin pin) {
        int likesCount = getLikesCount(pin.getId());
        pin.setLikeCount(likesCount);
        return pin;
    }
}