package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.likes.service.LikesService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class PinControllerLikeTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PinRepository pinRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private LikesService likesService;


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
//                            .with(csrf())
//                            .with(user("testuser").roles("USER"))  // 인증 사용자 추가
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
    @DisplayName("좋아요 저장 실패 - 존재하지 않는 핀")
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
                .andExpect(jsonPath("$.errorCode").value("2008"));

        // DB 검증
        Optional<Likes> likes = likesRepository.findByPinIdAndUserId(pinId, userId);
        assertThat(likes).isEmpty();
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
    @DisplayName("좋아요 취소 성공 - 좋아요 true")
    @Transactional
    void likesDeleteTSuccess() throws Exception {
        //given
        Long pinId = 1L;
        Long userId = 1L;
        String requestBody = "{\"userId\": " + userId + "}";
        int lcount = likesService.getLikesCount(pinId);

        // when & then
        mvc.perform(
                        delete("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))

                .andExpect(jsonPath("$.data.isLiked").value(false))
                .andExpect(jsonPath("$.data.likeCount").value(lcount - 1));

        // DB 검증
        Likes likes = likesRepository.findByPinIdAndUserId(pinId, userId)
                .orElseThrow(() -> new ServiceException(ErrorCode.LIKES_CREATE_FAILED));
        assertThat(likes.getLiked()).isFalse();
        assertThat(likes.getPin().getId()).isEqualTo(pinId);
        assertThat(likes.getUser().getId()).isEqualTo(userId);
        assertThat(likes.getModifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("좋아요 취소 성공 - 좋아요 false")
    @Transactional
    void likesDeleteFSuccess() throws Exception {
        //given
        Long pinId = 4L;
        Long userId = 1L;
        String requestBody = "{\"userId\": " + userId + "}";

        // when & then
        mvc.perform(
                        delete("/api/pins/{pinId}/likes", pinId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
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
        assertThat(likes.getModifiedAt()).isNotNull();
    }


    @Test
    @DisplayName("좋아요 토글 성공 - 좋아요 취소 -> 등록")
    @Transactional
    void likseToggleTF() throws Exception {
        // given
        Long pinId = 2L;
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
    @DisplayName("좋아요 개수 가져오기 성공 - 특정 핀 조회")
    void likeGetLikeCountTByPin() throws Exception {
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
                .andExpect(jsonPath("$.data.likeCount").value(likesRepository.countByPin_IdAndLikedTrue(pinId)));
    }

    @Test
    @DisplayName("좋아요 개수 가져오기 성공 - 취소된 핀")
    void likeGetLikeCountByPin() throws Exception {
        // given
        Long pinId = 4L;
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
                .andExpect(jsonPath("$.data.likeCount").value(0));
    }


    @Test
    @DisplayName("좋아요한 사용자 목록 조회 성공")
    void likesGetUsersWhoLikedPin() throws Exception {
        // given
        Long pinId = 1L;

        Integer[] userIds = likesRepository.findUsersByPinIdAndLikedTrue(pinId)
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
                .andExpect(jsonPath("$.data.length()").value(likesRepository.countByPin_IdAndLikedTrue(pinId)))
                .andExpect(jsonPath("$.data[*].id", containsInAnyOrder(userIds)));
    }

    @Test
    @DisplayName("좋아요한 사용자 목록 조회 성공 - 취소건")
    void likesGetUsersWhoLikedPinF() throws Exception {
        // given
        Long pinId = 4L;

        Integer[] userIds = likesRepository.findUsersByPinIdAndLikedTrue(pinId)
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
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.data[*].id", containsInAnyOrder(userIds)));
    }

}
