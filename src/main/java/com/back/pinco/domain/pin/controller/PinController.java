package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.bookmark.dto.BookmarkDto;
import com.back.pinco.domain.bookmark.dto.addBookmarkRequest;
import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.domain.likes.dto.*;
import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.CreatePinRequest;
import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.dto.UpdatePinContentRequest;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.rq.Rq;
import com.back.pinco.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pins")
public class PinController {

    private final PinService pinService;

    private final UserService userService;

    private final BookmarkService bookmarkService;

    private final LikesService likesService;

    private final Rq rq;

    //생성
    @PostMapping
    public RsData<PinDto> createPin(@Valid @RequestBody CreatePinRequest pinReqbody) {
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com");
        Pin pin = pinService.write(actor, pinReqbody);
        PinDto pinDto= new PinDto(pin);
        return new RsData<>(
                "200",
        "성공적으로 처리되었습니다",
                pinDto
        );
    }

    //조회
    //id로 조회
    @GetMapping("/{pinId}")
    public RsData<PinDto> getPinById(@PathVariable("pinId") Long pinId){
        Pin pin = pinService.findById(pinId);
        // pin 좋아요 개수 설정
        pin.setLikeCount(likesService.getLikesCount(pinId));

        PinDto pinDto = new PinDto(pin);

        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDto
        );
    }
    //범위로 조회
    @GetMapping
    public RsData<List<PinDto>> getRadiusPins(
            @NotNull
            @Min(-90)
            @Max(90)
            @RequestParam double latitude,
            @NotNull
            @Min(-180)
            @Max(180)
            @RequestParam double longitude
    ) {
        List<Pin> pins = pinService.findNearPins(latitude, longitude);

        List<PinDto> pinDtos = pins.stream()
                .map((pin)->{
                    pin.setLikeCount(likesService.getLikesCount(pin.getId()));
                    return new PinDto(pin);
                })
                .collect(Collectors.toList());

        if (pinDtos.isEmpty()) {
            return new RsData<>(
                    "204",
                    "조회된 값이 없습니다.",
                    null
            );
        }
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDtos
        );
    }

    //사용자로 조회
    @GetMapping("/user/{userId}")
    public RsData<List<PinDto>> getUserPins(
            @NotNull
            @PathVariable Long userId
    ){
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com");
        User writer = userService.findById(userId);
        List<Pin> pins = pinService.findByUserId(actor, writer);
        List<PinDto> pinDtos = pins.stream()
                .map((pin)->{
                    pin.setLikeCount(likesService.getLikesCount(pin.getId()));
                    return new PinDto(pin);
                })
                .collect(Collectors.toList());
        return new RsData<>(
                "200",
        "성공적으로 처리되었습니다",
                pinDtos
        );
    }

    //전부 조회
    @GetMapping("/all")
    public RsData<List<PinDto>> getAll() {
        List<Pin> pins = pinService.findAll();

        List<PinDto> pinDtos = pins.stream()
                .map((pin)->{
                    pin.setLikeCount(likesService.getLikesCount(pin.getId()));
                    return new PinDto(pin);
                })
                .toList();

        if (pins.isEmpty()) {
            return new RsData<>(
                    "204",
                    "조회된 값이 없습니다.",
                    null
            );
        }
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDtos
        );
    }

    //갱신
    //핀 내용 갱신
    @PutMapping(("/{pinId}"))
    public RsData<PinDto> updatePinContent(
            @PathVariable("pinId") Long pinId,
            @Valid @RequestBody UpdatePinContentRequest putPinReqbody
            ){
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com");
        Pin pin = pinService.update(actor, pinId, putPinReqbody);
        PinDto pinDto = new PinDto(pin);
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDto
        );
    }
    //공개 여부 갱신
    @PutMapping(("/{pinId}/public"))
    public RsData<PinDto> changePinPublic(
            @PathVariable("pinId") Long pinId
    ){
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com");
        Pin pin = pinService.changePublic(actor, pinId);
        PinDto pinDto = new PinDto(pin);
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                pinDto
        );
    }
    //삭제
    @DeleteMapping("/{pinId}")
    public RsData<Void> deletePin(@PathVariable Long pinId) {
        pinService.deleteById(pinId);
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                null
        );
    }


    // 좋아요 등록
    @PostMapping("/{pinId}/likes")
    public RsData<createPinLikesResponse> togglePinLikes(
            @PathVariable("pinId") Long pinId,
            @Valid @RequestBody createPinLikesRequest reqbody
    ) {
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                likesService.createPinLikes(pinId, reqbody.userId())
        );

    }

    // 좋아요 취소
    @DeleteMapping("/{pinId}/likes")
    public RsData<deletePinLikesResponse> togglePinLikes(
            @PathVariable("pinId") Long pinId,
            @Valid @RequestBody deletePinLikesRequest reqbody
    ) {
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                likesService.togglePinLikes(pinId, reqbody.userId())
        );
    }

    // 해당 핀을 좋아요 누른 유저 ID 목록 전달
    @GetMapping("{pinId}/likesusers")
    public RsData<List<PinLikedUserResponse>> getUsersWhoLikedPin(
            @PathVariable("pinId") Long pinId
    ) {
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다",
                likesService.getUsersWhoLikedPin(pinId)
        );
    }


    @Operation(summary = "해당 핀 북마크 추가", description = "특정 핀을 사용자의 북마크에 추가")
    @PostMapping("{pinId}/bookmarks")
    public RsData<BookmarkDto> addBookmark(
            @RequestBody addBookmarkRequest requestDto
    ) {
        User actor = rq.getActor();

        BookmarkDto bookmarkDto = bookmarkService.addBookmark(actor.getId(), requestDto.pinId());
        return new RsData<>(
                "200",
                "성공적으로 처리되었습니다.",
                bookmarkDto
        );
    }

}