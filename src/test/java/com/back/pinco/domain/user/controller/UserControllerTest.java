package com.back.pinco.domain.user.controller;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.repository.UserRepository;
import com.back.pinco.domain.user.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 실제 스프링 부트 애플리케이션 컨텍스트 로드
@AutoConfigureMockMvc(addFilters = true) // MockMvc 자동 구성
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
    @Transactional
    @DisplayName("회원 정보 조회")
    void t3() throws Exception {
        // given: 유저 생성
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        User saved = userRepository.save(new User(email, passwordEncoder.encode(rawPwd), "윤서"));

        // when: 로그인해서 토큰 받기
        MvcResult loginResult = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"%s","password":"%s"}
                        """.formatted(email, rawPwd)))
                .andExpect(status().isOk())
                .andReturn();

        // 토큰/키 꺼내기
        String body = loginResult.getResponse().getContentAsString();
        JsonNode root = om.readTree(body);
        String accessToken = root.at("/data/accessToken").asText();
        String apiKey      = root.at("/data/apiKey").asText();

        // then: 토큰을 헤더에 붙여서 호출 → rq.getActor()가 채워짐
        mvc.perform(get("/api/user/getInfo")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 정보를 성공적으로 조회했습니다."))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.userName").value("윤서"));
    }



    @Test
    @DisplayName("회원 정보 수정 - 회원 이름만 수정")
    @Transactional
    void t4() throws Exception {
        // given: 유저 생성
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        User saved = userRepository.save(new User(email, passwordEncoder.encode(rawPwd), "윤서"));

        // when: 로그인해서 토큰 받기
        MvcResult loginResult = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"%s","password":"%s"}
                    """.formatted(email, rawPwd)))
                .andExpect(status().isOk())
                .andReturn();

        // 토큰/키 꺼내기
        String body = loginResult.getResponse().getContentAsString();
        JsonNode root = om.readTree(body);
        String accessToken = root.at("/data/accessToken").asText();
        String apiKey = root.at("/data/apiKey").asText();

        // 요청 본문: 이름만 변경
        String newUserName = "윤서수정";
        String requestJson = """
        {
            "password": "%s",
            "newUserName": "%s",
            "newPassword": ""
        }
        """.formatted(rawPwd, newUserName);

        // then: 수정 요청
        mvc.perform(put("/api/user/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer " + apiKey + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // ✅ DB 검증
        User updated = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getUserName()).isEqualTo(newUserName);
        assertThat(passwordEncoder.matches(rawPwd, updated.getPassword())).isTrue(); // 비밀번호는 그대로
    }

    @Test
    @DisplayName("회원 정보 수정 - 비밀번호만 수정")
    @Transactional
    void t5() throws Exception {
        // given: 유저 생성
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        User saved = userRepository.save(new User(email, passwordEncoder.encode(rawPwd), "윤서"));

        // when: 로그인해서 토큰 받기
        MvcResult loginResult = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"%s","password":"%s"}
                    """.formatted(email, rawPwd)))
                .andExpect(status().isOk())
                .andReturn();

        // 토큰/키 꺼내기
        String body = loginResult.getResponse().getContentAsString();
        JsonNode root = om.readTree(body);
        String accessToken = root.at("/data/accessToken").asText();
        String apiKey = root.at("/data/apiKey").asText();

        // 비밀번호만 변경
        String newPassword = "12341234";
        String requestJson = """
        {
          "password": "%s",
          "newUserName": "",
          "newPassword": "%s"
        }
        """.formatted(rawPwd, newPassword);

        // then: 수정 요청
        mvc.perform(put("/api/user/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer " + apiKey + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // ✅ DB 검증 (해시 매칭으로 확인)
        User updated = userRepository.findById(saved.getId()).orElseThrow();
        // 새 비밀번호로 매칭되어야 함
        assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
        // 이전 비밀번호로는 매칭되면 안 됨
        assertThat(passwordEncoder.matches(rawPwd, updated.getPassword())).isFalse();
        // 이름은 그대로
        assertThat(updated.getUserName()).isEqualTo("윤서");
    }


    @Test
    @DisplayName("회원 정보 수정 - 비밀번호만 수정")
    @Transactional
    void t6() throws Exception {
        // given: 유저 생성
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        User saved = userRepository.save(new User(email, passwordEncoder.encode(rawPwd), "윤서"));

        // when: 로그인해서 토큰 받기
        MvcResult loginResult = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"email":"%s","password":"%s"}
                    """.formatted(email, rawPwd)))
                .andExpect(status().isOk())
                .andReturn();

        // 토큰/키 꺼내기
        String body = loginResult.getResponse().getContentAsString();
        JsonNode root = om.readTree(body);
        String accessToken = root.at("/data/accessToken").asText();
        String apiKey = root.at("/data/apiKey").asText();

        // 비밀번호만 변경
        String newUserName = "감자";
        String newPassword = "12341234";
        String requestJson = """
        {
          "password": "%s",
          "newUserName": "%s",
          "newPassword": "%s"
        }
        """.formatted(rawPwd, newUserName, newPassword);

        // then: 수정 요청
        mvc.perform(put("/api/user/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .header("Authorization", "Bearer " + apiKey + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원정보 수정 완료"));

        // ✅ DB 검증 (해시 매칭으로 확인)
        User updated = userRepository.findById(saved.getId()).orElseThrow();
        // 새 비밀번호로 매칭되어야 함
        assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
        // 이전 비밀번호로는 매칭되면 안 됨
        assertThat(passwordEncoder.matches(rawPwd, updated.getPassword())).isFalse();
        // 이름은 그대로
        assertThat(updated.getUserName()).isEqualTo(newUserName);
    }

    @Test
    @DisplayName("회원 탈퇴 - 로그인한 사용자, 비밀번호 확인 후 삭제")
    @Transactional
    void t7() throws Exception {
        // given: 유저 생성
        String email = "login+" + UUID.randomUUID() + "@example.com";
        String rawPwd = "Password123!";
        User saved = userRepository.save(new User(email, passwordEncoder.encode(rawPwd), "윤서"));
        Long id = saved.getId();

        // when: 로그인해서 토큰/키 받기
        MvcResult loginResult = mvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"email":"%s","password":"%s"}
            """.formatted(email, rawPwd)))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        JsonNode root = om.readTree(body);
        String accessToken = root.at("/data/accessToken").asText();
        String apiKey      = root.at("/data/apiKey").asText();

        // then: DELETE 호출 (비밀번호 바디 포함) → rq.getActor()로 인증 사용자 주입됨
        mvc.perform(delete("/api/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {"password":"%s"}
            """.formatted(rawPwd))
                        .header("Authorization", "Bearer " + apiKey + " " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("200"))
                .andExpect(jsonPath("$.msg").value("회원 탈퇴가 완료되었습니다."));

        // ✅ DB 검증: 삭제되었는지 확인
        assertThat(userRepository.findById(id)).isEmpty();
    }



    @Test
    @DisplayName("회원가입 실패 - 이메일 형식 오류")
    @Transactional
    void t8() throws Exception {
        // given: 잘못된 이메일(도메인 없음)
        String badEmail = "yunseo+" + UUID.randomUUID(); // @와 도메인 없음
        String rawPwd = "Password123!";

        String reqJson = """
      {
        "email": "%s",
        "userName": "윤서",
        "password": "%s"
      }
      """.formatted(badEmail, rawPwd);

        // when & then: 400 + 에러코드/메시지 확인
        mvc.perform(post("/api/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(reqJson)
                        .with(csrf())) // CSRF를 끄지 않았다면 유지, 껐다면 제거 가능
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("2001"))
                .andExpect(jsonPath("$.msg").value("이메일 형식이 올바르지 않습니다."));

        // ✅ DB 검증: 저장되지 않아야 함
        assertThat(userRepository.findByEmail(badEmail)).isEmpty();
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
        String email = "user1@example.com";
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
        String userName = "유저1";
        String rawPwd = "Password123!";
        String body = """
      {"email":"%s","userName":"유저1","password":"%s"}
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
        String email = "user1@example.com";
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

        mvc.perform(get("/api/user/getInfo", nonExistId)
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

        mvc.perform(delete("/api/user/delete/{id}", id)
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

}
