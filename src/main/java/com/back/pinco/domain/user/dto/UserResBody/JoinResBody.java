package com.back.pinco.domain.user.dto.UserResBody;

import com.back.pinco.domain.user.dto.UserDto;

import java.time.LocalDateTime;

public record JoinResBody(
        Long id,
        String email,
        String userName,
        LocalDateTime createdAt
) {
    public JoinResBody(UserDto userDto) {
        this(
                userDto.id(),
                userDto.email(),
                userDto.userName(),
                userDto.createdAt()
        );
    }
}

