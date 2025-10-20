package com.back.pinco.domain.likes.dto;

import com.back.pinco.domain.pin.entity.Pin;

import java.time.LocalDateTime;

/**
 * 사용자가 좋아요한 핀 목록
 * @param id 핀 ID
 * @param latitude 위도
 * @param longitude 경도
 * @param content 내용
 * @param userId 사용자 ID
 * @param pinTags 핀 태그
 * @param likeCount 좋아요 수
 * @param isPublic 공개여부
 * @param createdAt 생성일
 * @param modifiedAt 수정일
 */
public record PinsLikedByUserResponse(
        Long id,
        Double latitude,
        Double longitude,
        String content,
        Long userId,
//        List<PinTag> pinTags,
        int likeCount,
        Boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PinsLikedByUserResponse formEntry(Pin pin) {
        return new PinsLikedByUserResponse(
                pin.getId(),
                pin.getPoint().getY(),
                pin.getPoint().getX(),
                pin.getContent(),
                pin.getUser().getId(),
//                pin.getPinTags(),
                pin.getLikeCount(),
                pin.getIsPublic(),
                pin.getCreatedAt(),
                pin.getModifiedAt()
        );
    }

}
