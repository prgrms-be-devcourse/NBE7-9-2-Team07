package com.back.pinco.domain.user.dto.UserReqBody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginReqBody(
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {}
