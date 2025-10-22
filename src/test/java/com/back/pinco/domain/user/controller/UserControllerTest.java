package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.back.pinco.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // 실제 스프링 부트 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc(addFilters = false) // MockMvc 자동 구성
class UserControllerIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;


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
    @DisplayName("회원 조회 성공 - id로 User 반환")
    @Transactional
    void t3() throws Exception {
        // given
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String hashed = passwordEncoder.encode(rawPwd);

        User seed = new User(email, hashed, "윤서");
        User saved = userRepository.save(seed);
        Long id = saved.getId();

        // when & then
        mvc.perform(get("/api/user/getInfo/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // RsData 래핑 확인(선택)
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 정보를 성공적으로 조회했습니다."))
                // 실제 데이터는 $.data 아래에 있음
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.userName").value("윤서"));

        // ✅ DB 검증
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
        String newName = "알감자";
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
        // given
        String email = "delete+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String hashedPwd = passwordEncoder.encode(rawPwd);
        User seed = new User(email, hashedPwd, "윤서");
        User saved = userRepository.save(seed);
        Long id = saved.getId();

        // JSON body
        String body = """
    {
      "password": "%s"
    }
    """.formatted(rawPwd);

        // when & then
        mvc.perform(delete("/api/user/delete/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."));

        // DB 검증
        assertThat(userRepository.findById(id)).isEmpty();
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }


    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    @Transactional
    void t8() throws Exception {
        String email = "yunseo+" + UUID.randomUUID() + "";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"윤서","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2001"))
                .andExpect(jsonPath("$.msg").value("이메일 형식이 올바르지 않습니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 형식 오류")
    @Transactional
    void t9() throws Exception {
        String email = "yunseo+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "";
        String body = """
      {"email":"%s","userName":"윤서","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2002"))
                .andExpect(jsonPath("$.msg").value("비밀번호 형식을 만족하지 않습니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    @Test
    @DisplayName("회원가입 실패 - 회원 이름 형식 오류")
    @Transactional
    void t10() throws Exception {
        String email = "yunseo+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"냠","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2003"))
                .andExpect(jsonPath("$.msg").value("회원 이름 형식을 만족하지 않습니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일로 가입")
    @Transactional
    void t11() throws Exception {
        String email = "yoonseo@example.com";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"냠냠","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("2004"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 이메일입니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 회원 이름으로 가입")
    @Transactional
    void t12() throws Exception {
        String email = "example1@example.com";
        String userName = "감자";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"감자","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("2005"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 회원이름입니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByUserName(userName)).isPresent();
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    @Transactional
    void t13() throws Exception {
        String email = "example1@example.com";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"감자","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("2006"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 이메일입니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isEmpty();
    }

    @Test
    @DisplayName("로그인 실패 - 틀린 비밀번호")
    @Transactional
    void t14() throws Exception {
        String email = "yoonseo@example.com";
        String userName = "감자";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"감자","password":"%s"}
      """.formatted(email, rawPwd);

        mvc.perform(post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("2007"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));

        // ✅ DB 검증
        assertThat(userRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("회원정보 조회 실패 - 존재하지 않는 ID")
    @Transactional
    void t15() throws Exception {
        long nonExistId = 999999L; // DB에 없는 ID라고 가정

        mvc.perform(get("/api/user/getInfo/{id}", nonExistId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // ✅ 404
                .andExpect(jsonPath("$.errorCode").value("2008")) // ✅ ErrorCode.USER_INFO_NOT_FOUND (가정)
                .andExpect(jsonPath("$.msg").value("회원 정보를 찾을 수 없습니다.")); // ✅ 메시지 확인
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 입력 비밀번호 불일치")
    @Transactional
    void t16() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        // when: 틀린 현재 비밀번호로 수정 시도
        String body = """
      {
        "password": "WrongPass!",              
        "newUserName": "새닉네임",
        "newPassword": "NewPassword123!"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                // then: 401 + PASSWORD_NOT_MATCH(2006)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("2007"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 이름 형식 오류")
    @Transactional
    void t17() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "새",
        "newPassword": ""
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2003"))
                .andExpect(jsonPath("$.msg").value("회원 이름 형식을 만족하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 이미 존재하는 이름")
    @Transactional
    void t18() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "감자",
        "newPassword": ""
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("2005"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 회원이름입니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 비밀번호 형식 오류")
    @Transactional
    void t19() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "",
        "newPassword": "1234"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2002"))
                .andExpect(jsonPath("$.msg").value("비밀번호 형식을 만족하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 회원 정보 모두 수정 시 회원 이름 형식 오류")
    @Transactional
    void t20() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "감",
        "newPassword": "12341234"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2003"))
                .andExpect(jsonPath("$.msg").value("회원 이름 형식을 만족하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 회원 정보 모두 수정 시 회원 이름 중복")
    @Transactional
    void t21() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "감자",
        "newPassword": "12341234"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("2005"))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 회원이름입니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 회원 정보 모두 수정 시 비밀번호 형식 오류")
    @Transactional
    void t22() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "감자칩",
        "newPassword": "1234"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2002"))
                .andExpect(jsonPath("$.msg").value("비밀번호 형식을 만족하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원정보 수정 실패 - 수정 사항 없음")
    @Transactional
    void t23() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "CorrectPass123!",              
        "newUserName": "윤서",
        "newPassword": "CorrectPass123!"
      }
      """;

        mvc.perform(put("/api/user/edit/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("2011"))
                .andExpect(jsonPath("$.msg").value("변경할 내용이 없습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    @Transactional
    void t24() throws Exception {
        // given: 유저 생성
        String email = "user+" + UUID.randomUUID() + "@test.com";
        String oldPwd = "CorrectPass123!";
        String oldName = "윤서";
        User saved = userService.createUser(email, oldPwd, oldName);
        Long id = saved.getId();

        String body = """
      {
        "password": "Correct123!"
      }
      """;

        mvc.perform(delete("/api/user/delete", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("2007"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));

        // and: DB 검증 - 이름/비번 변경되지 않아야 함
        User after = userRepository.findById(id).orElseThrow();
        assertThat(after.getUserName()).isEqualTo(oldName);
        assertThat(passwordEncoder.matches(oldPwd, after.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("NewPassword123!", after.getPassword())).isFalse();
    }

    // --- 디버깅용: JWT payload 디코드 (Base64URL) ---
    private static String decodeJwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return "<invalid-jwt-format>";
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "<decode-error: " + e.getMessage() + ">";
        }
    }

    // JWT payload 디코드(서명검증 X, 디버깅용)
    private static String payloadOf(String jwt) {
        try {
            String[] p = jwt.split("\\.");
            return new String(Base64.getUrlDecoder().decode(p[1]), StandardCharsets.UTF_8);
        } catch (Exception e) { return "<decode-error:" + e.getMessage() + ">"; }
    }

    @Test
    @DisplayName("로그인 → 로그인 응답의 쿠키(apiKey, accessToken) 그대로 전달하여 /api/user/mypage(200)")
    @Transactional
    void t25() throws Exception {
        // 1) 로그인
        var login = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          {"email":"user1@example.com","password":"12345678"}
                        """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        // 2) 로그인 응답의 Set-Cookie 에서 그대로 꺼내기 (이게 제일 안전함)
        var cookiesFromLogin = login.getResponse().getCookies();
        // 참고: 여기엔 name=apiKey, name=accessToken 두 개가 들어있음 (둘 다 HttpOnly)

        // 3) 쿠키 그대로 들고 마이페이지 호출 (헤더 불필요)
        mvc.perform(get("/api/user/mypage")
                        .cookie(cookiesFromLogin)            // ✅ 쿠키 그대로 전달
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user1@example.com"));
    }
}
