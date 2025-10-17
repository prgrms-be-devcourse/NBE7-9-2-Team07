package com.back.pinco.domain.tag.dto;

import com.back.pinco.domain.pin.entity.Pin;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 📍 태그 기반 필터링 결과 DTO
 * 지도 마커 + 게시물 미리보기 카드용
 */
public record GetFilteredPinResponse(
        Long id,               // 핀 ID
        double latitude,       // 위도
        double longitude,      // 경도
        String content,        // 게시물 내용 (요약)
        int likeCount,         // 좋아요 수
        String userNickname,   // 작성자 닉네임
        List<String> tags,      // 연결된 태그 목록
        LocalDateTime createdAt, // 생성일
        LocalDateTime modifiedAt // 수정일
) {
    public GetFilteredPinResponse(Pin pin) {
        this(
                pin.getId(),
                pin.getPoint().getY(),    // latitude
                pin.getPoint().getX(),    // longitude
                pin.getContent(),
                pin.getLikeCount(),
                pin.getUser() != null ? pin.getUser().getUserName() : "알 수 없음",
                pin.getPinTags().stream()
                        .map(pt -> pt.getTag().getKeyword())
                        .toList(),
                pin.getCreatedAt(),
                pin.getModifiedAt()
        );
    }
}
