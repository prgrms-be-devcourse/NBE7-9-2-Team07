package com.back.pinco.domain.post.dto;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String content,
        LocalDateTime createAt,
        LocalDateTime modifiedAt,
        Pin pin
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.getPin()
        );
    }
}
