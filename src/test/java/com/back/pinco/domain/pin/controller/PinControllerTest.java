package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class PinControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private LikesRepository likesRepository;

    long targetId = 1L; //일단 내 DB에 맞춰뒀음. 추후 수정 필요
    long failedTargetId = Integer.MAX_VALUE;

    @Test
    @DisplayName("핀 생성")
    void t1_1() throws Exception {

        double lat = 0;
        double lon = 0;
        String content = "new Content!";

        String jsonContent = String.format(
                """
                        {
                            "content": "%s",
                            "latitude" : %s, 
                            "longitude" : %s
                        }
                        """, content, lat, lon
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/api/pins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("createPin"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.latitude").value(lat))
                .andExpect(jsonPath("$.data.longitude").value(lon))
                .andExpect(jsonPath("$.data.content").value(content));
    }

    @Test
    @DisplayName("핀 생성 - 실패 (경도 정보 오류)")
    void t1_2() throws Exception {

        double lat = 0;

        String content = "new Content!";

        String jsonContent = String.format(
                """
                        {
                            "content": "%s",
                            "latitude" : %s
                        }
                        """, content, lat
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/api/pins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("createPin"))
                .andExpect(jsonPath("$.errorCode").value("1007"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("핀 생성 - 실패 (경도 정보 오류)")
    void t1_3() throws Exception {

        double lon = 0;
        String content = "new Content!";

        String jsonContent = String.format(
                """
                        {
                            "content": "%s",
                            "longitude" : %s
                        }
                        """, content, lon
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/api/pins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("createPin"))
                .andExpect(jsonPath("$.errorCode").value("1006"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("핀 생성 - 실패 (내용 정보 오류)")
    void t1_4() throws Exception {

        double lat = 0;
        double lon = 0;

        String jsonContent = String.format(
                """
                        {
                            "latitude" : %s,
                            "longitude" : %s
                        }
                        """, lat, lon
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/api/pins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("createPin"))
                .andExpect(jsonPath("$.errorCode").value("1005"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("id로 핀 조회 - 성공")
    void t2_1() throws Exception {

        Pin pin = pinRepository.findById(targetId).get();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/%s".formatted(targetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pin.getId()))
                .andExpect(jsonPath("$.data.latitude").value(pin.getPoint().getY()))
                .andExpect(jsonPath("$.data.longitude").value(pin.getPoint().getX()))
                .andExpect(jsonPath("$.data.createdAt").value(matchesPattern(pin.getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.modifiedAt").value(matchesPattern(pin.getModifiedAt().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.data.pinTags.length()").value(pin.getPinTags().size()))
        ;
    }

    @Test
    @DisplayName("id로 핀 조회 - 실패 (id가 없음)")
    void t2_2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/%s".formatted(failedTargetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("id로 핀 조회 - 실패 (있긴 한데 삭제되어서 안 뜸)")
    void t2_3() throws Exception {
        ResultActions resultActions1 = mvc
                .perform(
                        delete("/api/pins/%s".formatted(targetId))
                )
                .andDo(print());
        ResultActions resultActions2 = mvc
                .perform(
                        get("/api/pins/%s".formatted(targetId))
                )
                .andDo(print());

        resultActions2
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("특정 지점에서 범위 내 좌표 확인")
    void t3_1() throws Exception {

        Pin pin = pinRepository.findById(targetId).get();
        List<Pin> pins = pinRepository.findPinsWithinRadius(pin.getPoint().getX(),pin.getPoint().getY(),1000.0,true,false);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins")
                                .param("radius", String.valueOf(1000))
                                .param("longitude", String.valueOf(pin.getPoint().getX()))
                                .param("latitude", String.valueOf(pin.getPoint().getY()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getRadiusPins"))
                .andExpect(status().isOk());

        for (int i = 0; i < pins.size(); i++) {
            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(pins.get(i).getId()))
                    .andExpect(jsonPath("$.data[%d].latitude".formatted(i)).value(pins.get(i).getPoint().getY()))
                    .andExpect(jsonPath("$.data[%d].longitude".formatted(i)).value(pins.get(i).getPoint().getX()))
                    .andExpect(jsonPath("$.data[%d].createdAt".formatted(i)).value(matchesPattern(pins.get(i).getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data[%d].modifiedAt".formatted(i)).value(matchesPattern(pins.get(i).getModifiedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data[%d].pinTags.length()".formatted(i)).value(pins.get(i).getPinTags().size()));
        }

    }

    @Test
    @DisplayName("특정 지점에서 범위 내 핀 확인 - 핀 없음")
    void t3_2() throws Exception {
        // 핀이 없는 위치와 반경을 설정합니다.
        double outOfRangeLat = 0;
        double outOfRangeLon = 0;
        double radius = 10; // 10미터

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins")
                                .param("radius", String.valueOf(radius))
                                .param("latitude", String.valueOf(outOfRangeLat))
                                .param("longitude", String.valueOf(outOfRangeLon))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getRadiusPins"))
                .andExpect(jsonPath("$.errorCode").value("1003"))
                .andExpect(jsonPath("$.msg").exists());
    }


    @Test
    @DisplayName("모든 핀 리턴")
    void t4() throws Exception {
        List<Pin> pins = pinRepository.findByIsPublicAndDeleted(true, false);

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/all")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isOk());

        for (int i = 0; i < pins.size(); i++) {
            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(pins.get(i).getId()))
                    .andExpect(jsonPath("$.data[%d].latitude".formatted(i)).value(pins.get(i).getPoint().getY()))
                    .andExpect(jsonPath("$.data[%d].longitude".formatted(i)).value(pins.get(i).getPoint().getX()))
                    .andExpect(jsonPath("$.data[%d].createdAt".formatted(i)).value(matchesPattern(pins.get(i).getCreatedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data[%d].modifiedAt".formatted(i)).value(matchesPattern(pins.get(i).getModifiedAt().toString().replaceAll("0+$", "") + ".*")))
                    .andExpect(jsonPath("$.data[%d].pinTags.length()".formatted(i)).value(pins.get(i).getPinTags().size()));
        }
    }

    @Test
    @DisplayName("핀 내용 수정")
    void t5_1_1() throws Exception {

        String content = "updated Content!";
        Pin pin = pinRepository.findById(targetId).get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/%s".formatted(targetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("updatePinContent"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").value(targetId))
                .andExpect(jsonPath("$.data.latitude").value(pin.getPoint().getY()))
                .andExpect(jsonPath("$.data.longitude").value(pin.getPoint().getX()))
                .andExpect(jsonPath("$.data.content").value(content));
    }

    @Test
    @DisplayName("핀 내용 수정 - 실패 (id없음)")
    void t5_1_2() throws Exception {

        String content = "updated Content!";
        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/%s".formatted(failedTargetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("updatePinContent"))
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("핀 내용 수정 - 실패 (내용 없음)")
    void t5_1_3() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/%s".formatted(targetId)).contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("updatePinContent"))
                .andExpect(jsonPath("$.errorCode").value("1005"))
                .andExpect(jsonPath("$.msg").exists());
    }


    @Test
    @DisplayName("핀 공개 여부 수정")
    void t5_2_1() throws Exception {

        Pin pin = pinRepository.findById(targetId).get();

        String expectedIsPublicString = String.valueOf(!pin.getIsPublic());

        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/%s/public".formatted(targetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("changePinPublic"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").value(targetId))
                .andExpect(jsonPath("$.data.isPublic").value(expectedIsPublicString));
    }

    @Test
    @DisplayName("핀 공개 여부 수정 - 실패 (id 없음)")
    void t5_2_2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/%s/public".formatted(failedTargetId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("changePinPublic"))
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").exists());

    }

    @Test
    @DisplayName("핀 삭제 - 성공")
    void t6_1() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/pins/%s".formatted(targetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("deletePin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("핀 삭제 - 실패 (id없음)")
    void t6_2() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/pins/%s".formatted(failedTargetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("deletePin"))
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").exists());
    }


    @Test
    @DisplayName("좋아요 저장 성공")
    @Transactional
    void likesCreateSuccess() throws Exception {
        //given
        Long pinId = 5L;
        Long userId = 2L;
        String requestBody = "{\"userId\": " + userId + "}";

        // when & then
        mvc.perform(
                post("/api/pins/{pinId}/likes", pinId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
                            .with(csrf())
                            .with(user("testuser").roles("USER"))  // 인증 사용자 추가
                )
                .andDo(print())
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))

                .andExpect(jsonPath("$.data.isLiked").value(true))
                .andExpect(jsonPath("$.data.likeCount").value(1));

        // DB 검증
        Likes likes = likesRepository.findByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_CREATE_FAILED));
        assertThat(likes.getLiked()).isTrue();
        assertThat(likes.getPin().getId()).isEqualTo(pinId);
        assertThat(likes.getUser().getId()).isEqualTo(userId);
        assertThat(likes.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("좋아요 저장 실패 - 존재하지 않는 Pin")
    @Transactional
    void likesCreatefailPinId() throws Exception {
        // given
        Long pinId = 99999L;
        Long userId = 2L;
        String requestBody = "{\"userId\": " + userId + "}";

        // when & then
        mvc.perform(
                        post("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .with(csrf())
                                .with(user("testuser").roles("USER"))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("1002"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 핀입니다."));

        // DB 검증
        Optional<Likes> likes = likesRepository.findByPinIdAndUserId(pinId, userId);
        assertThat(likes).isEmpty();
    }

    @Test
    @DisplayName("좋아요 저장 실패 - 존재하지 않는 사용자")
    @Transactional
    void likesCreatefailUserId() throws Exception {
        // given
        Long pinId = 2L;
        Long userId = 999L;
        String requestBody = "{\"userId\": " + userId + "}";

        // when & then
        mvc.perform(
                        post("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .with(csrf())
                                .with(user("testuser").roles("USER"))
                )
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("2008"))
                .andExpect(jsonPath("$.msg").value("회원 정보를 찾을 수 없습니다."));

        // DB 검증
        Optional<Likes> likes = likesRepository.findByPinIdAndUserId(pinId, userId);
        assertThat(likes).isEmpty();
    }

    @Test
    @DisplayName("좋아요 토글 - 좋아요 취소 -> 등록 성공")
    @Transactional
    void likseToggleTF() throws Exception {
        // given - 실제 데이터 저장
        Long pinId = 1L;
        Long userId = 1L;
        String requestBody = "{\"userId\": " + userId + "}";

        // when & then
        mvc.perform(
                        post("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .with(csrf())
                                .with(user("testuser").roles("USER"))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.isLiked").value(false));

        // DB 검증
        Likes likes = likesRepository.findByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_CREATE_FAILED));
        assertThat(likes.getLiked()).isFalse();


        // 좋아요 재등록
        mvc.perform(
                        delete("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .with(csrf())
                                .with(user("testuser").roles("USER"))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.isLiked").value(true));

        // DB 검증
        likes = likesRepository.findByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_CREATE_FAILED));
        assertThat(likes.getLiked()).isTrue();
    }

    @Test
    @DisplayName("좋아요 저장 실패 - DB 예외 발생")    // 실패 -
    @Transactional
    void likesCreatefailDB() throws Exception {
//        // given
//        Long pinId = 5L;
//        Long userId = 2L;
//        String requestBody = "{\"userId\": " + userId + "}";
//
//        // Repository mock으로 예외 발생 시뮬레이션
//        doThrow(new DataAccessException("DB 연결 실패") {
//        })
//                .when(likesRepository).save(any(Likes.class));
//
//        // when & then
//        mvc.perform(
//                        post("/api/pins/{pinId}/likes", pinId)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(requestBody)
//                                .with(csrf())
//                                .with(user("testuser").roles("USER"))
//                )
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.errorCode").value("500"))
//                .andExpect(jsonPath("$.msg").exists());
//
//        // DB에 저장되지 않았는지 검증
//        Optional<Likes> likes = likesRepository.findByPinIdAndUserId(pinId, userId);
//        assertThat(likes).isEmpty();
    }


    @Test
    @DisplayName("좋아요 저장 실패 - 트랜잭션 롤백") // 미구현
    @Transactional
    void likePinTransactionRollbackTest() {
//        // given
//        Long pinId = 5L;
//        Long userId = 2L;
//
//        // when
//        assertThrows(RuntimeException.class, () -> {
//            Likes likes = Likes.builder()
//                    .pin(pinRepository.findById(pinId).orElseThrow())
//                    .user(userRepository.findById(userId).orElseThrow())
//                    .isLiked(true)
//                    .build();
//
//            likesRepository.save(likes);
//
//            // 강제로 예외 발생
//            throw new RuntimeException("트랜잭션 롤백 테스트");
//        });
//
//        // then - 롤백되어 저장되지 않았는지 검증
//        Optional<Likes> likes = likesRepository.findByPinIdAndUserId(pinId, userId);
//        assertThat(likes).isEmpty();
    }


    @Test
    @DisplayName("좋아요 개수 가져오기 - 특정 핀 조회")
    void likeGetLikeCountByPin() throws Exception {
        // given
        Long pinId = 1L;
        Pin pin = pinRepository.findById(pinId).orElseThrow();

        // when & then
        mvc.perform(
                        get("/api/pins/{pinId}", pinId)
                )
                .andDo(print())
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))

                .andExpect(jsonPath("$.data.id").value(pin.getId()))
                .andExpect(jsonPath("$.data.likeCount").value(likesRepository.countByPin_Id(pinId)));

    }


    @Test
    @DisplayName("특정 핀의 좋아요 사용자 목록 조회 성공")
    void likesGetUsersWhoLikedPin() throws Exception {
        // given
        Long pinId = 1L;

        Integer[] userIds = likesRepository.findUsersByPinId(pinId)
                .stream()
                .map(User::getId)
                .map(id -> id.intValue())
                .toArray(Integer[]::new);

        // when & then
        mvc.perform(
                        get("/api/pins/{pinId}/likesusers", pinId)
                )
                .andDo(print())
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getUsersWhoLikedPin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))

                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(likesRepository.countByPin_Id(pinId)))
                .andExpect(jsonPath("$.data[*].id", containsInAnyOrder(userIds)));
    }

}
