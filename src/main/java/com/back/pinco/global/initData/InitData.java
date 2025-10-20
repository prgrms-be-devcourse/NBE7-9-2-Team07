package com.back.pinco.global.initData;

import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.PostPinReqbody;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.service.PinService;
import com.back.pinco.domain.tag.entity.PinTag;
import com.back.pinco.domain.tag.entity.Tag;
import com.back.pinco.domain.tag.service.PinTagService;
import com.back.pinco.domain.tag.service.TagService;
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
    private final BookmarkService bookmarkService;
    private final LikesService likesService;
    private final TagService tagService;
    private final PinTagService pinTagService;

    @Bean
    ApplicationRunner baseInitData() {
        return args -> {
            self.work();
        };
    }

    @Transactional
    public void work() {
//        if (pinService.count() > 0) return;
        /**
         * application.yml의 ddl-auto: create 설정으로
         * 애플리케이션 시작 시 초기 데이터로 재생성 -> 시퀀스도 초기화
         */

        double baseLat = 37.5665; // ✅ 서울시청 기준 위도
        double baseLng = 126.9780; // ✅ 서울시청 기준 경도

        User user1 = userService.createUser("user1@example.com", "12345678", "유저1");
        User user2 = userService.createUser("user2@example.com", "12341234", "유저2");

        // ✅ 시청 기준 반경 1km 이내 임의 좌표
        Pin pinA = pinService.write(user1, new PostPinReqbody(baseLat + 0.0012, baseLng + 0.0015, "서울 시청 근처 카페 ☕"));
        Pin pinB = pinService.write(user1, new PostPinReqbody(baseLat - 0.0008, baseLng + 0.0010, "덕수궁 돌담길 산책 중 🌳"));
        Pin pinC = pinService.write(user1, new PostPinReqbody(baseLat + 0.0006, baseLng - 0.0013, "청계천 산책로 발견 👣"));
        Pin pinD = pinService.write(user2, new PostPinReqbody(baseLat - 0.0005, baseLng - 0.0010, "광화문에서 커피 한 잔 ☕"));
        Pin pinE = pinService.write(user2, new PostPinReqbody(baseLat + 0.0003, baseLng + 0.0002, "서울시청 옆 공원 벤치 휴식 🍃"));


        // ✅ 샘플 북마크 생성 (user1이 pinA, pinD 북마크 / user2가 pinB 북마크)
        bookmarkService.addBookmark(user1.getId(), pinA.getId());
        bookmarkService.addBookmark(user1.getId(), pinD.getId());
        bookmarkService.addBookmark(user2.getId(), pinB.getId());


        // 좋아요 등록 (user1이 pin1, pin2 좋아요 / user2가 pin1 좋아요)
        likesService.toggleLike(pinA, user1);
        likesService.toggleLike(pinB, user1);
        likesService.toggleLike(pinA, user2);

        // 샘플 태그 등록
        Tag t1 = tagService.createTag("카페");
        Tag t2 = tagService.createTag("감성");
        Tag t3 = tagService.createTag("반려동물");
        Tag t4 = tagService.createTag("데이트");
        Tag t5 = tagService.createTag("야경");
        Tag t6 = tagService.createTag("산책로");
        Tag t7 = tagService.createTag("전망좋은");

        // 샘플 핀-태그 연결 (PinTag)
        PinTag pt1 = pinTagService.createPinTag(pinA, t1);
        PinTag pt2 = pinTagService.createPinTag(pinA, t2);
        PinTag pt3 = pinTagService.createPinTag(pinA, t4);
        PinTag pt4 = pinTagService.createPinTag(pinB, t2);
        PinTag pt5 = pinTagService.createPinTag(pinB, t3);
        PinTag pt6 = pinTagService.createPinTag(pinC, t5);
        PinTag pt7 = pinTagService.createPinTag(pinC, t6);
        PinTag pt8 = pinTagService.createPinTag(pinD, t4);
        PinTag pt9 = pinTagService.createPinTag(pinD, t5);
        PinTag pt10 = pinTagService.createPinTag(pinD, t7);
        PinTag pt11 = pinTagService.createPinTag(pinE, t2);
        PinTag pt12 = pinTagService.createPinTag(pinE, t1);
    }

}
