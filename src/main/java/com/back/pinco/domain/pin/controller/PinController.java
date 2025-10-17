package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.dto.PinDto;
import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.dto.PutPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pins")
public class PinController {

    @Autowired
    private PinService pinService;

    @Autowired
    private UserService userService;

    //검증 예외처리 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handlePinValidationException(MethodArgumentNotValidException e) {
        FieldError firstError = e.getBindingResult().getFieldError();

        ErrorCode errorCode = ErrorCode.INVALID_PIN_CONTENT;
        if(firstError.getField().equals("latitude")) {errorCode = ErrorCode.INVALID_PIN_LATITUDE;}
        else if(firstError.getField().equals("longitude")) {errorCode = ErrorCode.INVALID_PIN_LONGITUDE;}

        return ResponseEntity
                .status(errorCode.getStatus()) // HTTP 400 Bad Request
                .body(new RsData<>(
                        String.valueOf(errorCode.getCode()),
                        errorCode.getMessage()
                ));
    }

    //생성
    @PostMapping
    public RsData<PinDto> createPin(@Valid @RequestBody PostPinReqbody pinReqbody) {
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com").get();
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
                .map(PinDto::new)
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

    //전부 조회
    @GetMapping("/all")
    public RsData<List<PinDto>> getAll() {
        List<Pin> pins = pinService.findAll();

        List<PinDto> pinDtos = pins.stream()
                .map(PinDto::new)
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
            @Valid @RequestBody PutPinReqbody putPinReqbody
            ){
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com").get();
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
    public RsData<PinDto> chagePinPublic(
            @PathVariable("pinId") Long pinId
    ){
        //jwt 구현 후 변경 예정. 일단 id 1번 넣음
        User actor = userService.findByEmail("user1@example.com").get();
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



}