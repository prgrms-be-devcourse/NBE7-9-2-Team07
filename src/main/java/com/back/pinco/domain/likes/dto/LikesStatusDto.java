package com.back.pinco.domain.likes.dto;

public record LikesStatusDto(
        boolean isLiked,    // 사용자의 좋아요 여부
        int likeCount       // 해당 포스트의 총 좋아요 개수
) {};
