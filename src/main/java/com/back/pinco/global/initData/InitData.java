package com.back.pinco.global.initData;

import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
public class InitData {

    @Autowired
    @Lazy
    private InitData self;
    private final PinService pinService;
    private final UserService userService;
    private final LikesService likesService;


    @Bean
    ApplicationRunner baseInitData() {
        return args -> {
            self.work();
            self.createLikes(); // 좋아요 등록
        };
    }

    @Transactional
    public void work() {
        if (pinService.count() > 0) return;

        double baseLat = 37.5665; // ✅ 서울시청 기준 위도
        double baseLng = 126.9780; // ✅ 서울시청 기준 경도

        User user1 = userService.createUser("user1@example.com", "12345678", "유저1");
        User user2 = userService.createUser("user2@example.com", "12341234", "유저2");

        // ✅ 시청 기준 반경 1km 이내 임의 좌표
        Pin pin1 = pinService.write(user1, new PostPinReqbody( baseLat + 0.0012, baseLng + 0.0015,"서울 시청 근처 카페 ☕"));
        Pin pin2 = pinService.write(user1, new PostPinReqbody(  baseLat - 0.0008, baseLng + 0.0010,"덕수궁 돌담길 산책 중 🌳"));
        Pin pin3 = pinService.write(user1, new PostPinReqbody(  baseLat + 0.0006, baseLng - 0.0013,"청계천 산책로 발견 👣"));
        Pin pin4 = pinService.write(user2, new PostPinReqbody(  baseLat - 0.0005, baseLng - 0.0010,"광화문에서 커피 한 잔 ☕"));
        Pin pin5 = pinService.write(user2, new PostPinReqbody( baseLat + 0.0003, baseLng + 0.0002,"서울시청 옆 공원 벤치 휴식 🍃"));
    }

    public void createLikes() {
        if (likesService.count() > 0) return;

        User user1 = userService.findByEmail("user1@example.com").get();
        User user2 = userService.findByEmail("user2@example.com").get();

        Pin pin1 = pinService.write(user1, new PostPinReqbody( 23.0, 100.0,"좋아요 테스트1"));
        Pin pin2 = pinService.write(user2, new PostPinReqbody(  34.0, 100.0,"좋아요 테스트2"));

        // 좋아요 등록
        likesService.saveUserLikesPin(user1, pin1);
        likesService.saveUserLikesPin(user1, pin2);
        likesService.saveUserLikesPin(user2, pin1);


    }

}
