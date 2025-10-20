package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.entity.Pin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 실제 스프링 부트 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc(addFilters = false) // MockMvc 자동 구성
class UserControllerLikeTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    LikesRepository likesRepository;


    @Test
    @DisplayName("특정 사용자가 좋아요 누른 핀 목록 전달")
    @Transactional
    void getPinsLikedByUser() throws Exception {
        // given
        Long userId = 1L;

        Integer[] pinIds = likesRepository.findPinsByUserIdAndLikedTrue(userId)
                .stream()
                .map(Pin::getId)
                .map(id -> id.intValue())
                .toArray(Integer[]::new);

        // when & then
        mvc.perform(
                        get("/api/user/{userId}/likespins", userId)
                )
                .andDo(print())
                .andExpect(handler().handlerType(UserController.class))
                .andExpect(handler().methodName("getPinsLikedByUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("성공적으로 처리되었습니다"))

                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(likesRepository.countByUser_idAndLikedTrue(userId)))
                .andExpect(jsonPath("$.data[*].id", containsInAnyOrder(pinIds)));;
    }
}
