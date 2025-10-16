package com.back.pinco.domain.likes.service;

import com.back.pinco.global.geometry.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final GeometryUtil geometryUtil;


    // 해당 pin의 좋아요 개수 전달
    public int getLikesCount(Long pinId) {
        // 전달 받은 ID를 key로 사용하여 개수 조회
        return 2;
    }


    // 좋아요 여부 확인
    private boolean isLikedByUser(Long pinId, Long userId) {
        return true;
    }

    // 좋아요 상태 전달 DTO
    record LikesStatusDto(
            boolean isLiked,    // 사용자의 좋아요 여부
            int likeCount       // 해당 포스트의 총 좋아요 개수
    ) {};

    // 좋아요 토글
    public LikesStatusDto toggleLike(Long pinId, Long userId) {
        // 좋아요 등록 로직 구현

        return new LikesStatusDto(true,4);
    }


    // 해당 핀에 좋아요 누른 유저 ID 목록 전달
    public Optional<List<Long>> getUsersWhoLikedPin(Long pinId) {
        return Optional.of (List.of(80L, 81L, 82L));
    }


    // 해당 유저가 좋아요 누른 핀 ID 목록 전달
    public Optional<List<Long>> getPinsLikedByUser(Long userId) {
        return Optional.of(List.of(90L, 91L, 92L));
    }

}
