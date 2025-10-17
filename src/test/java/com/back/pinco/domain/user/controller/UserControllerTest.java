package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 실제 스프링 부트 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc(addFilters = false) // MockMvc 자동 구성
class UserControllerIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 - DB에 유저 저장 및 RsData 반환")
    @Transactional
    void t1() throws Exception {
        String email = "yunseo+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"윤서","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 가입이 완료되었습니다"));

        // ✅ DB 검증
        User saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getUserName()).isEqualTo("윤서");
        // 비밀번호는 해시가 저장되어야 함
        assertThat(passwordEncoder.matches(rawPwd, saved.getPassword())).isTrue();
    }


    @Test
    @DisplayName("로그인 성공")
    @Transactional
    void t2() throws Exception {
        // given: 로그인 가능한 사용자 사전 저장
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String hashed = passwordEncoder.encode(rawPwd);

        User seed = new User(email, hashed, "윤서");
        userRepository.save(seed);

        String body = """
          { "email":"%s", "password":"%s" }
          """.formatted(email, rawPwd);

        // when & then
        mvc.perform(post("/api/user/login") // 클래스 레벨 @RequestMapping("/api/user") 가정
                        .with(csrf())                         // 시큐리티 켠 상태면 필요
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("로그인 성공"));
    }

    @Test
    @DisplayName("회원 조회 성공 - email로 User 반환")
    @Transactional
    void t3() throws Exception {
        // given
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String hashed = passwordEncoder.encode(rawPwd);

        User seed = new User(email, hashed, "윤서");
        User saved = userRepository.save(seed);
        Long id = saved.getId();

        // when & then (컨트롤러가 Optional<User>를 그대로 반환한다고 가정)
        mvc.perform(get("/api/user/check/{id}", id)
                        .param("email", email)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.userName").value("윤서"));

        // ✅ DB 검증: 여전히 존재해야 함
        User fromDb = userRepository.findById(id).orElseThrow();
        assertThat(fromDb.getEmail()).isEqualTo(email);
        assertThat(fromDb.getUserName()).isEqualTo("윤서");
        assertThat(passwordEncoder.matches(rawPwd, fromDb.getPassword())).isTrue();
    }

    @Test
    @DisplayName("회원 정보 수정 - 비밀번호만 수정")
    @Transactional
    void t4() throws Exception {
        // given
        String email = "edit+" + UUID.randomUUID() + "@example.com";
        String oldRaw = "Password123!";
        String oldHashed = passwordEncoder.encode(oldRaw);
        String name = "윤서";

        User saved = userRepository.save(new User(email, oldHashed, name));
        Long id = saved.getId();

        String newRaw = "NewPassword123!";

        // newUserName은 비우고(이름은 그대로), newPassword만 채운다
        String body = """
      {
        "email": "%s",
        "password": "%s",
        "newUserName": "",
        "newPassword": "%s"
      }
      """.formatted(email, oldRaw, newRaw);

        // when & then
        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // DB 검증: 비번만 바뀌고 이름은 유지
        User updated = userRepository.findById(id).orElseThrow();
        assertThat(updated.getUserName()).isEqualTo(name);
        assertThat(passwordEncoder.matches(newRaw, updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(oldRaw, updated.getPassword())).isFalse();
        assertThat(updated.getPassword()).isNotEqualTo(oldHashed);
    }


    @Test
    @DisplayName("회원 정보 수정 - 비밀번호만 수정")
    @Transactional
    void t5() throws Exception {
        // given
        String email = "edit+" + UUID.randomUUID() + "@example.com";
        String oldRaw = "Password123!";
        String oldHashed = passwordEncoder.encode(oldRaw);
        String name = "윤서";

        User seed = new User(email, oldHashed, name);
        User saved = userRepository.save(seed);
        Long id = saved.getId();

        String newRaw = "NewPassword123!";

        String body = """
      {
        "email": "%s",
        "password": "%s",
        "newUserName": "",
        "newPassword": "%s"
      }
      """.formatted(email, oldRaw, newRaw);

        // when
        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // then
        User updated = userRepository.findByEmail(email).get();

        // 1) 새 비번으로 매칭되어야 함
        assertThat(passwordEncoder.matches(newRaw, updated.getPassword())).isTrue();

        // 2) 옛 비번으로는 더 이상 매칭되면 안 됨
        assertThat(passwordEncoder.matches(oldRaw, updated.getPassword())).isFalse();

        // 3) 해시 자체도 바뀌어야 함
        assertThat(updated.getPassword()).isNotEqualTo(oldHashed);

        // 이름은 그대로 유지
        assertThat(updated.getUserName()).isEqualTo(name);
    }

    @Test
    @DisplayName("회원 정보 수정 - 이름과 비밀번호 동시 수정")
    @Transactional
    void t6() throws Exception {
        // given
        String email = "edit+" + UUID.randomUUID() + "@example.com";
        String oldRaw = "Password123!";
        String oldHashed = passwordEncoder.encode(oldRaw);
        String oldName = "윤서";
        String newName = "감자";
        String newRaw = "NewPassword123!";

        User saved = userRepository.save(new User(email, oldHashed, oldName));
        Long id = saved.getId();

        String body = """
      {
        "email": "%s",
        "password": "%s",
        "newUserName": "%s",
        "newPassword": "%s"
      }
      """.formatted(email, oldRaw, newName, newRaw);

        // when
        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // then
        User updated = userRepository.findByEmail(email).get();

        assertThat(updated.getUserName()).isEqualTo(newName);
        assertThat(passwordEncoder.matches(newRaw, updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(oldRaw, updated.getPassword())).isFalse();
        assertThat(updated.getPassword()).isNotEqualTo(oldHashed);
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 삭제 후 존재하지 않음")
    @Transactional
    void t7() throws Exception {
        // given: 유저 한 명을 심는다
        String email = "delete+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String hashedPwd = passwordEncoder.encode(rawPwd);
        User seed = new User(email, hashedPwd, "윤서");
        User saved = userRepository.save(seed);
        Long id = saved.getId();

        // when & then: 삭제 요청
        mvc.perform(delete("/api/user/delete/{id}", id)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // RsData 직렬화 키에 맞춰 선택
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."));

        // DB에서 실제로 삭제되었는지 확인
        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }
}
