package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.user.dto.UserDto;
import com.back.pinco.domain.user.dto.UserReqBody.EditReqBody;
import com.back.pinco.domain.user.dto.UserReqBody.JoinReqBody;
import com.back.pinco.domain.user.dto.UserReqBody.LoginReqBody;
import com.back.pinco.domain.user.dto.UserResBody.JoinResBody;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public RsData<JoinResBody> join(
            @RequestBody @Valid JoinReqBody reqBody) {

        User user = userService.createUser(reqBody.email(),reqBody.password(),reqBody.userName());
        return new RsData<> (
                "200",
                "회원 가입이 완료되었습니다",
                new JoinResBody(new UserDto(user))
        );
    }

    @PostMapping("/login")
    // 인증인가 추가할 때 반환타입 LoginResBody 로 수정
    public RsData<Void> login(
            @RequestBody @Valid LoginReqBody reqBody
    ) {
        boolean loginCheck = userService.login(reqBody.email(), reqBody.password());
        if(loginCheck) {
            return new RsData<>(
                    "200",
                    "로그인 성공"
            );
        } else {
            return new RsData<>(
                    "401",
                    "로그인 실패"
            );
        }
    }

    @GetMapping("/check/{id}")
    public Optional<User> check(
            @PathVariable Long id
    ) {
        return userService.userInform(id);
    }

    @PutMapping("/edit/{id}")
    public RsData<Void> edit(
            @RequestBody @Valid EditReqBody reqBody,
            @PathVariable Long id
    ) {
        User currentUser = userService.findById(id).get();
        if(userService.checkPwd(currentUser, reqBody.password())) {
            boolean nameChanged = reqBody.newUserName() != null && !reqBody.newUserName().isBlank();
            boolean pwdChanged = reqBody.newPassword() != null && !reqBody.newPassword().isBlank();

            if (nameChanged && pwdChanged) {
                userService.editAll(currentUser, reqBody.newUserName(), reqBody.newPassword());
            } else if (nameChanged) {
                userService.editName(currentUser, reqBody.newUserName());
            } else if (pwdChanged) {
                userService.editPwd(currentUser, reqBody.newPassword());
            }
            return new RsData<>(
                    "200",
                    "회원정보 수정 완료"
            );
        } else {
            return new RsData<>(
                    "401-1",
                    "회원정보 수정 실패"
            );
        }
    }

    @DeleteMapping("/delete/{id}")
    public RsData<Void> delete(
            @PathVariable Long id
    ) {
        User user = userService.findById(id).get();
        userService.delete(user);
        if(!userService.checkExist(user.getEmail())) {
            return new RsData<>(
                    "200",
                    "회원 탈퇴가 완료되었습니다."
            );
        } else {
            return new RsData<>(
                    "401",
                    "회원 탈퇴에 실패했습니다."
            );
        }
    }
}
