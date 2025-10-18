package com.back.pinco.domain.user.dto.UserReqBody;

public record JoinReqBody(
        String email,
        String password,
        String userName
) {}
