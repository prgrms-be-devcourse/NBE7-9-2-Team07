package com.back.pinco.domain.likes.service;

import com.back.pinco.domain.likes.dto.LikesStatusDto;
import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.geometry.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final GeometryUtil geometryUtil;
    private final PinService pinService;
    private final UserService userService;
    private final LikesRepository likesRepository;


    /**
     * 해당 핀의 좋아요 개수 반환
     * @param pinId   핀 ID
     * @return int 좋아요 개수
     */
    public int getLikesCount(Long pinId) {
        if (!likesRepository.existsByPinId(pinId)) {
            return 0;
        }

        return (int) likesRepository.countByPinId(pinId);
    }

    /**
     * 해당 핀에 대해 사용자의 좋아요 저장 여부 확인
     * @param pinId    핀 ID
     * @param userId   사용자 ID
     * @return boolean 좋아요 존재 여부
     */
    private boolean existByLikes(Long pinId, Long userId) {
        return likesRepository.existsByPinIdAndUserId(pinId, userId);
    }


    /**
     * 좋아요 상태를 토글하고, 현재 좋아요 상태와 해당 핀의 총 좋아요 개수를 반환
     * @param pin
     * @param user
     * @return LikesStatusDto
     */
    public LikesStatusDto toggleLike(Pin pin, User user) {
        if (!existByLikes(pin.getId(), user.getId())) {
            // 신규 좋아요
            likesRepository.save(new Likes(user, pin));
            return new LikesStatusDto(true, 1);
        }

        // 기존에 있다면
        Likes likes = likesRepository.findByPinIdAndUserId(pin.getId(), user.getId()).get();
        likes.toggleLike();
        likesRepository.save(likes);

        return new LikesStatusDto(likes.getIsLiked(), getLikesCount(pin.getId()));
    }

    /**
     * 해당 핀에 좋아요 누른 유저 ID 목록 전달
     * @param pinId
     * @return
     */
    public Optional<List<Long>> getUsersWhoLikedPin(Long pinId) {

        return Optional.of (List.of(80L, 81L, 82L));
    }


    // 해당 유저가 좋아요 누른 핀 ID 목록 전달
    public Optional<List<Long>> getPinsLikedByUser(Long userId) {
        return Optional.of(List.of(90L, 91L, 92L));
    }

}
