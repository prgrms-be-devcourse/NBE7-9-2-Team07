package com.back.pinco.domain.post.dto;

import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        String content,
        LocalDateTime createAt,
        LocalDateTime modifiedAt,
        PinDto pin
) {
    public PostDto(Post post) {
        this(
                post.getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                new PinDto(post.getPin())
        );
    }
}
