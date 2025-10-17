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
     * 특정 핀에 대한 좋아요 수 조회
     *
     * @param pinId 핀 ID
     * @return 좋아요 수
     */
    public long getLikesCount(Long pinId) {
        return likesRepository.countByPinId(pinId);
    }

    /**
     * 특정 (핀, 사용자)에 대한 좋아요 존재 여부 확인
     *
     * @param pinId 핀 ID
     * @param userId 사용자 ID
     * @return 존재하면 true, 없으면 false
     */
    private boolean existByLikes(Long pinId, Long userId) {
        return likesRepository.existsByPinIdAndUserId(pinId, userId);
    }

    /**
     * 특정 핀에 대해 사용자의 좋아요 상태를 토글
     * 좋아요 없으면 생성, 있으면 상태 변경
     *
     * @param pin 좋아요를 토글할 핀 엔티티
     * @param user 좋아요를 토글할 사용자 엔티티
     * @return 토글 후의 좋아요 상태와 총 좋아요 수 DTO
     */
    public LikesStatusDto toggleLike(Pin pin, User user) {
        if (!existByLikes(pin.getId(), user.getId())) {
            likesRepository.save(new Likes(user, pin));
            return new LikesStatusDto(true, 1);
        }

        Likes likes = likesRepository.findByPinIdAndUserId(pin.getId(), user.getId()).get();
        likes.toggleLike();
        likesRepository.save(likes);

        return new LikesStatusDto(likes.getIsLiked(), (int) getLikesCount(pin.getId()));
    }


    public Optional<List<Long>> getUsersWhoLikedPin(Long pinId) {
        return Optional.of (List.of(80L, 81L, 82L));
    }


    // 해당 유저가 좋아요 누른 핀 ID 목록 전달
    public Optional<List<Long>> getPinsLikedByUser(Long userId) {
        return Optional.of(List.of(90L, 91L, 92L));
    }

}
