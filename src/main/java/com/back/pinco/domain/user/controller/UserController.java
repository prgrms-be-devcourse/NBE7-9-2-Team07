package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.bookmark.repository.BookmarkRepository;
import com.back.pinco.domain.likes.dto.PinsLikedByUserResponse;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.dto.UserDto;
import com.back.pinco.domain.user.dto.UserReqBody.*;
import com.back.pinco.domain.user.dto.UserResBody.GetInfoResponse;
import com.back.pinco.domain.user.dto.UserResBody.JoinResponse;
import com.back.pinco.domain.user.dto.UserResBody.MyPageResponse;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import com.back.pinco.global.rq.Rq;
import com.back.pinco.global.rsData.RsData;
import com.back.pinco.global.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final LikesService likesService;
    private final PinRepository pinRepository;
    private final BookmarkRepository bookmarkRepository;
    private final LikesRepository likesRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final Rq rq;

    @PostMapping("/join")
    public RsData<JoinResponse> join(
            @RequestBody JoinRequest reqBody
    ) {
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
    public RsData<Map<String, String>> login(
            @RequestBody LoginRequest reqBody
    ) {
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
    public RsData<Map<String, String>> reissue(
            @RequestBody Map<String, String> body
    ) {
        String refreshToken = body.getOrDefault("refreshToken", "");
        if (refreshToken.isBlank() || !jwtTokenProvider.isValid(refreshToken)) {
            throw new ServiceException(ErrorCode.INVALID_ACCESS_TOKEN);
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
        User user = rq.getActor();
        return new RsData<>(
                "200",
                "회원 정보를 성공적으로 조회했습니다.",
                new GetInfoResponse(new UserDto(user))
        );
    }

    @PutMapping("/edit")
    public RsData<Void> edit(
            @RequestBody EditRequest reqBody
    ) {
        User currentUser = rq.getActor();
        userService.checkPwd(currentUser, reqBody.password());
        userService.editUserInfo(currentUser.getId(), reqBody.newUserName(), reqBody.newPassword());
        return new RsData<>(
                "200",
                "회원정보 수정 완료"
        );
    }

    @DeleteMapping("/delete")
    public RsData<Void> delete(
            @RequestBody DeleteRequest reqBody
    ) {
        User user = rq.getActor();
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
    public RsData<List<PinsLikedByUserResponse>> getPinsLikedByUser(
            @PathVariable("userId") Long userId
    ) {
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다", likesService.getPinsLikedByUser(userId)
        );
    }

    @GetMapping("/mypage")
    public RsData<MyPageResponse> myPage() {
        // 1) 로그인 사용자
        User user = rq.getActor();
        if (user == null) {
            throw new ServiceException(ErrorCode.AUTH_REQUIRED);
        }

        // 2) 내가 작성한 핀 목록 & 개수(리스트의 사이즈)
        List<Pin> myPins = pinRepository.findAccessibleByUser(user.getId(), user.getId());
        List<PinDto> myPinDtos = myPins.stream()
                .map(PinDto::new)
                .toList();

        // 3) 내가 북마크한(삭제되지 않은) 핀 목록 & 개수(리스트의 사이즈)
        List<Bookmark> bookmarks = bookmarkRepository.findByUserAndDeletedFalse(user);
        List<PinDto> bookmarkedPinDtos = bookmarks.stream()
                .map(b -> new PinDto(b.getPin()))
                .toList();

        Map<Long, Integer> likeCountsByPinId = new HashMap<>();
        for (PinDto p : myPinDtos) {
            likeCountsByPinId.put(p.id(),
                    (int) likesRepository.countByPin_IdAndLikedTrue(p.id()));
        }
        for (PinDto p : bookmarkedPinDtos) {
            likeCountsByPinId.computeIfAbsent(p.id(), id ->
                    (int) likesRepository.countByPin_IdAndLikedTrue(id));
        }


        // 4) 내가 지금까지 받은 총 '좋아요 수' (각 핀별 liked=true 카운트를 합산)
        long totalLikesReceived = myPins.stream()
                .mapToLong(pin -> likesRepository.countByPin_IdAndLikedTrue(pin.getId()))
                .sum();

        return new RsData<>(
                "200",
                "마이페이지 조회 성공",
                new MyPageResponse(
                        new UserDto(user), myPinDtos, bookmarkedPinDtos, totalLikesReceived)
                );
    }
}

