package com.back.pinco.domain.user.dto.UserReqBody;

public record LoginReqBody(
        String email,
        String password
) {}
