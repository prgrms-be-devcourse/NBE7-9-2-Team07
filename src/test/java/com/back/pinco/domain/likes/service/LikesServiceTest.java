package com.back.pinco.domain.likes.service;

import com.back.pinco.domain.likes.repository.LikesRepository;
import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class LikesServiceTest {

    @Autowired private PinService pinService;
    @Autowired private UserService userService;
    @Autowired private LikesService likesService;

    @Autowired private LikesRepository likesRepository;


    @Test
    @DisplayName("좋아요 신규 등록 테스트")
    void t1() {
        // given
        long rowCount = likesRepository.count();

        double baseLat = 37.5665; // ✅ 서울시청 기준 위도
        double baseLng = 126.9780; // ✅ 서울시청 기준 경도

        User user1 = userService.createUser("user1@test.com", "pwd", "테스트1");
        User user2 = userService.createUser("user2@test.com", "pwd", "테스트2");

        // ✅ 시청 기준 반경 1km 이내 임의 좌표
        Pin pin1 = pinService.write(user1, new PostPinReqbody( baseLat + 1, baseLng + 2,"핀1"));
        Pin pin2 = pinService.write(user1, new PostPinReqbody(  baseLat - 1, baseLng + 3,"핀2"));

        // when
        likesService.createPinLikes(pin1.getId(), user1.getId());
        likesService.createPinLikes(pin2.getId(), user1.getId());
        likesService.createPinLikes(pin1.getId(), user2.getId());


        // then
        assertThat(likesRepository.count()).isEqualTo(rowCount + 3);
    }
}
