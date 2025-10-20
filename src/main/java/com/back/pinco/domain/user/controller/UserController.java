package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.user.dto.UserDto;
import com.back.pinco.domain.user.dto.UserReqBody.DeleteRequest;
import com.back.pinco.domain.user.dto.UserReqBody.EditRequest;
import com.back.pinco.domain.user.dto.UserReqBody.JoinRequest;
import com.back.pinco.domain.user.dto.UserReqBody.LoginRequest;
import com.back.pinco.domain.user.dto.UserResBody.GetInfoResponse;
import com.back.pinco.domain.user.dto.UserResBody.JoinResponse;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public RsData<JoinResponse> join(
            @RequestBody @Valid JoinRequest reqBody) {

        User user = userService.createUser(reqBody.email(),reqBody.password(),reqBody.userName());
        return new RsData<> (
                "200",
                "회원 가입이 완료되었습니다",
                new JoinResponse(new UserDto(user))
        );
    }

    @PostMapping("/login")
    // 인증인가 추가할 때 반환타입 LoginResBody 로 수정
    public RsData<Void> login(
            @RequestBody @Valid LoginRequest reqBody
    ) {
       userService.login(reqBody.email(), reqBody.password());
       return new RsData<>(
               "200",
               "로그인 성공"
       );
    }

    @GetMapping("/getInfo/{id}")
    public RsData<GetInfoResponse> getUserInfo(
            @PathVariable Long id
    ) {
        User user = userService.userInform(id);
        return new RsData<>(
                "200",
                "회원 정보를 성공적으로 조회했습니다.",
                new GetInfoResponse(new UserDto(user))
        );
    }


    @PutMapping("/edit/{id}")
    public RsData<Void> edit(
            @RequestBody @Valid EditRequest reqBody,
            @PathVariable Long id
    ) {
        User currentUser = userService.findById(id);
        userService.checkPwd(currentUser, reqBody.password());
        userService.editUserInfo(currentUser, reqBody.newUserName(), reqBody.newPassword());
        return new RsData<>(
                "200",
                "회원정보 수정 완료"
        );
    }

    @DeleteMapping("/delete/{id}")
    public RsData<Void> delete(
            @RequestBody DeleteRequest reqBody,
            @PathVariable Long id
    ) {
        User user = userService.findById(id);
        userService.checkPwd(user, reqBody.password());
        userService.delete(user);
        return new RsData<>(
                "200",
                "회원 탈퇴가 완료되었습니다."
        );
    }
}
