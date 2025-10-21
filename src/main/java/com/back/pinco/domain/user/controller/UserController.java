package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.likes.dto.PinsLikedByUserResponse;
import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.user.dto.UserDto;
import com.back.pinco.domain.user.dto.UserReqBody.*;
import com.back.pinco.domain.user.dto.UserResBody.GetInfoResponse;
import com.back.pinco.domain.user.dto.UserResBody.JoinResponse;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.rq.Rq;
import com.back.pinco.global.rsData.RsData;
import com.back.pinco.global.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final LikesService likesService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Rq rq;

    @PostMapping("/join")
    public RsData<JoinResponse> join(@RequestBody @Valid JoinRequest reqBody) {
        User user = userService.createUser(reqBody.email(), reqBody.password(), reqBody.userName());
        String apiKey = userService.ensureApiKey(user);
        String access = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUserName());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId());

        rq.setCookie("apiKey", apiKey);
        rq.setCookie("accessToken", access);

        return new RsData<>(
                "200",
                "회원 가입이 완료되었습니다",
                new JoinResponse(new UserDto(user))
        );
    }

    @PostMapping("/login")
    public RsData<Map<String, String>> login(@RequestBody @Valid LoginRequest reqBody) {
        userService.login(reqBody.email(), reqBody.password());
        User user = userService.findByEmail(reqBody.email());

        // apiKey 보장
        String apiKey = userService.ensureApiKey(user);
        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUserName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 쿠키
        rq.setCookie("apiKey", apiKey);
        rq.setCookie("accessToken", accessToken);

        return new RsData<>(
                "200",
                "로그인 성공",
                Map.of(
                        "apiKey", apiKey,
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                )
        );
    }

    @PostMapping("/reissue")
    public RsData<Map<String, String>> reissue(@RequestBody Map<String, String> body) {
        String refreshToken = body.getOrDefault("refreshToken", "");
        if (refreshToken.isBlank() || !jwtTokenProvider.isValid(refreshToken)) {
            return new RsData<>(
                    "401",
                    "유효하지 않은 리프레시 토큰입니다.");
        }
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userService.findById(userId);

        String newAccess = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getUserName());
        String newRefresh = jwtTokenProvider.generateRefreshToken(user.getId());

        rq.setCookie("accessToken", newAccess);
        rq.setHeader("Authorization", "Bearer " + user.getApiKey() + " " + newAccess);

        return new RsData<>(
                "200",
                "재발급 성공",
                Map.of(
                "apiKey", user.getApiKey(),
                "accessToken", newAccess,
                "refreshToken", newRefresh
                )
        );
    }

    @GetMapping("/getInfo")
    public RsData<GetInfoResponse> getUserInfo() {
        User user = userService.requireActor();
        return new RsData<>(
                "200",
                "회원 정보를 성공적으로 조회했습니다.",
                new GetInfoResponse(new UserDto(user))
        );
    }

    @PutMapping("/edit")
    public RsData<Void> edit(
            @RequestBody EditRequest reqBody) {
        User currentUser = userService.requireActor();
        userService.checkPwd(currentUser, reqBody.password());
        userService.editUserInfo(currentUser, reqBody.newUserName(), reqBody.newPassword());
        return new RsData<>(
                "200",
                "회원정보 수정 완료"
        );
    }

    @DeleteMapping("/delete")
    public RsData<Void> delete(
            @RequestBody DeleteRequest reqBody) {
        User user = userService.requireActor();
        userService.checkPwd(user, reqBody.password());
        userService.delete(user);
        rq.deleteCookie("accessToken");
        rq.deleteCookie("apiKey");
        return new RsData<>(
                "200",
                "회원 탈퇴가 완료되었습니다."
        );
    }

    @GetMapping("/{userId}/likespins")
    public RsData<List<PinsLikedByUserResponse>> getPinsLikedByUser(@PathVariable("userId") Long userId) {
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다", likesService.getPinsLikedByUser(userId)
        );
    }
}

