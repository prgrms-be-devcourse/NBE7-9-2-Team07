package com.back.pinco.global.initData;

import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.post.service.PostService;
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
    private final PostService postService;
    private final UserService userService;


    @Bean
    ApplicationRunner baseInitData() {
        return args -> {
            self.work();
        };
    }

    @Transactional
    public void work() {
        if (pinService.count() > 0) return;

        double baseLat = 37.5665; // ✅ 서울시청 기준 위도
        double baseLng = 126.9780; // ✅ 서울시청 기준 경도

        User user1 = userService.createUser("user1@example.com", "유저1", "12345678");
        User user2 = userService.createUser("user2@example.com", "유저2", "12341234");

        // ✅ 시청 기준 반경 1km 이내 임의 좌표
        pinService.write(user1, new PostPinReqbody( baseLat + 0.0012, baseLng + 0.0015,"서울 시청 근처 카페 ☕"));
        pinService.write(user1, new PostPinReqbody(  baseLat - 0.0008, baseLng + 0.0010,"덕수궁 돌담길 산책 중 🌳"));
        pinService.write(user1, new PostPinReqbody(  baseLat + 0.0006, baseLng - 0.0013,"청계천 산책로 발견 👣"));
        pinService.write(user2, new PostPinReqbody(  baseLat - 0.0005, baseLng - 0.0010,"광화문에서 커피 한 잔 ☕"));
        pinService.write(user2, new PostPinReqbody( baseLat + 0.0003, baseLng + 0.0002,"서울시청 옆 공원 벤치 휴식 🍃"));


    }

}
