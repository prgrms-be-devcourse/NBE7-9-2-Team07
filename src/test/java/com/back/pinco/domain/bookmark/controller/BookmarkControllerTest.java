package com.back.pinco.domain.bookmark.controller;

import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.bookmark.repository.BookmarkRepository;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class BookmarkControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired PinRepository pinRepository;
    @Autowired BookmarkRepository bookmarkRepository;

    long failedTargetId = Long.MAX_VALUE;

    @Test
    @DisplayName("t1_1. 북마크 생성 성공")
    void t1_1() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        Pin pinC = pinRepository.findAll().stream()
                .filter(p -> "청계천 산책로 발견 👣".equals(p.getContent()))
                .findFirst().orElseThrow();

        Long targetPinId = pinC.getId();

        String jsonContent = String.format("{\n  \"userId\": %d,\n  \"pinId\": %d\n}", user1.getId(), targetPinId);

        ResultActions resultActions = mvc.perform(
                post("/api/pins/{pinId}/bookmarks", targetPinId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.pin.id").value(targetPinId.intValue()))
                .andExpect(jsonPath("$.errorCode").value("200"));

        assertThat(bookmarkRepository.findByUserAndPinAndDeletedFalse(user1, pinC)).isPresent();
    }

    @Test
    @DisplayName("t1_2. 북마크 생성 실패 (이미 북마크된 핀)")
    void t1_2() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();

        Long targetPinId = pinA.getId();

        String jsonContent = String.format("{\n  \"userId\": %d,\n  \"pinId\": %d\n}", user1.getId(), targetPinId);

        ResultActions resultActions = mvc.perform(
                post("/api/pins/{pinId}/bookmarks", targetPinId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        ).andDo(print());

        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("4002"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t1_3. 북마크 생성 실패 (존재하지 않는 사용자 ID)")
    void t1_3() throws Exception {
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();
        Long targetPinId = pinA.getId();

        String jsonContent = String.format("{\n  \"userId\": %d,\n  \"pinId\": %d\n}", failedTargetId, targetPinId);

        ResultActions resultActions = mvc.perform(
                post("/api/pins/{pinId}/bookmarks", targetPinId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("2006"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t2_1. 나의 북마크 목록 조회 성공")
    void t2_1() throws Exception {
        // 사용자 조회만 하고, controller는 Rq로 사용자 식별을 하므로 파라미터는 제거
        userRepository.findByEmail("user1@example.com").orElseThrow();

        ResultActions resultActions = mvc.perform(
                get("/api/bookmarks")
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("t2_2. 나의 북마크 목록 조회 성공 (북마크 없음)")
    void t2_2() throws Exception {
        userRepository.save(new User("nobody+" + Math.random() + "@example.com", "hashed", "노바디"));

        ResultActions resultActions = mvc.perform(
                get("/api/bookmarks")
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("t2_3. 나의 북마크 목록 조회 실패 (존재하지 않는 사용자 ID)")
    void t2_3() throws Exception {
        ResultActions resultActions = mvc.perform(
                get("/api/bookmarks")
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("2006"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t3_1. 북마크 삭제 성공 (soft delete)")
    void t3_1() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();
        Bookmark bookmark1A = bookmarkRepository.findByUserAndPinAndDeletedFalse(user1, pinA).orElseThrow();

        Long targetBookmarkId = bookmark1A.getId();

        ResultActions resultActions = mvc.perform(
                delete("/api/bookmarks/{bookmarkId}", targetBookmarkId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"));

        Bookmark deletedBookmark = bookmarkRepository.findById(targetBookmarkId).orElseThrow();
        assertThat(deletedBookmark.getDeleted()).isTrue();
    }

    @Test
    @DisplayName("t3_2. 북마크 삭제 실패 (존재하지 않는 북마크 ID)")
    void t3_2() throws Exception {
        ResultActions resultActions = mvc.perform(
                delete("/api/bookmarks/{bookmarkId}", failedTargetId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("4001"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t3_3. 북마크 삭제 실패 (소유자가 아님)")
    void t3_3() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        userRepository.findByEmail("user2@example.com").orElseThrow();
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();
        Bookmark bookmark1A = bookmarkRepository.findByUserAndPinAndDeletedFalse(user1, pinA).orElseThrow();

        Long targetBookmarkId = bookmark1A.getId();

        // 다른 사용자의 요청으로 삭제를 시도하면 소유자 검증에 의해 실패해야 함
        ResultActions resultActions = mvc.perform(
                delete("/api/bookmarks/{bookmarkId}", targetBookmarkId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("4001"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t3_4. 북마크 복원 성공")
    void t3_4() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();
        // 기존 북마크를 t삭제 상태로 만들어 놓기
        Bookmark bookmark1A = bookmarkRepository.findByUserAndPinAndDeletedFalse(user1, pinA).orElseThrow();
        bookmark1A.setDeleted();
        bookmarkRepository.save(bookmark1A);

        Long targetBookmarkId = bookmark1A.getId();

        ResultActions resultActions = mvc.perform(
                patch("/api/bookmarks/{bookmarkId}", targetBookmarkId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"));

        Bookmark restored = bookmarkRepository.findById(targetBookmarkId).orElseThrow();
        assertThat(restored.getDeleted()).isFalse();
    }

    @Test
    @DisplayName("t3_5. 북마크 복원 실패 (존재하지 않는 북마크 ID)")
    void t3_5() throws Exception {
        ResultActions resultActions = mvc.perform(
                patch("/api/bookmarks/{bookmarkId}", failedTargetId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("4001"))
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("t3_6. 북마크 복원 실패 (소유자가 아님)")
    void t3_6() throws Exception {
        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        userRepository.findByEmail("user2@example.com").orElseThrow();
        Pin pinA = pinRepository.findAll().stream()
                .filter(p -> "서울 시청 근처 카페 ☕".equals(p.getContent()))
                .findFirst().orElseThrow();
        Bookmark bookmark1A = bookmarkRepository.findByUserAndPinAndDeletedFalse(user1, pinA).orElseThrow();

        // 소유자가 다르도록 북마크를 삭제 상태로 만들어 놓기
        bookmark1A.setDeleted();
        bookmarkRepository.save(bookmark1A);

        Long targetBookmarkId = bookmark1A.getId();

        ResultActions resultActions = mvc.perform(
                patch("/api/bookmarks/{bookmarkId}", targetBookmarkId)
                        .with(csrf())
        ).andDo(print());

        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("4001"))
                .andExpect(jsonPath("$.msg").exists());
    }
}
