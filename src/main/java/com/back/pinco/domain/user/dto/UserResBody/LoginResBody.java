package com.back.pinco.domain.user.dto.UserResBody;

import com.back.pinco.domain.user.dto.UserDto;

public record LoginResBody(
        Long id,
        String email,
        String userName
) {
    public LoginResBody(UserDto userDto) {
        this(
                userDto.id(),
                userDto.email(),
                userDto.userName()
        );
    }
}
