package com.back.pinco.global.initData;

import com.back.pinco.domain.bookmark.service.BookmarkService;
import com.back.pinco.domain.likes.service.LikesService;
import com.back.pinco.domain.pin.dto.CreatePinRequest;
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
        User user3 = userService.createUser("no@example.com", "12345678", "노미경");

        // ✅ 시청 기준 반경 1km 이내 임의 좌표
        Pin pinA = pinService.write(user1, new CreatePinRequest(baseLat + 0.0012, baseLng + 0.0015, "서울 시청 근처 카페 ☕"));
        Pin pinB = pinService.write(user1, new CreatePinRequest(baseLat - 0.0008, baseLng + 0.0010, "덕수궁 돌담길 산책 중 🌳"));
        Pin pinC = pinService.write(user1, new CreatePinRequest(baseLat + 0.0006, baseLng - 0.0013, "청계천 산책로 발견 👣"));
        Pin pinD = pinService.write(user2, new CreatePinRequest(baseLat - 0.0005, baseLng - 0.0010, "광화문에서 커피 한 잔 ☕"));
        Pin pinE = pinService.write(user2, new CreatePinRequest(baseLat + 0.0003, baseLng + 0.0002, "서울시청 옆 공원 벤치 휴식 🍃"));

        Pin pin6 = pinService.write(user3, new CreatePinRequest(37.56652851254232, 126.99170316409894, "김밥 맛집"));
        Pin pin7 = pinService.write(user3, new CreatePinRequest(37.56552838426607, 127.00861365307968, "환승 지옥"));
        Pin pin8 = pinService.write(user3, new CreatePinRequest(37.548877786806514, 126.98935132168111, "1차 방문"));
        Pin pin9 = pinService.write(user3, new CreatePinRequest(37.548778723722855, 126.9898832031673, "2차 방문"));
        Pin pin10 = pinService.write(user3, new CreatePinRequest(37.54874263496653, 126.9893287082637, "3차 방문"));
        Pin pin11 = pinService.write(user3, new CreatePinRequest(37.57759977323471, 126.97689730646299, "경복궁"));


        // ✅ 샘플 북마크 생성 (user1이 pinA, pinD 북마크 / user2가 pinB 북마크)
        bookmarkService.addBookmark(user1.getId(), pinA.getId());
        bookmarkService.addBookmark(user1.getId(), pinD.getId());
        bookmarkService.addBookmark(user2.getId(), pinB.getId());

        bookmarkService.addBookmark(user1.getId(), pin6.getId());
        bookmarkService.addBookmark(user1.getId(), pin7.getId());
        bookmarkService.addBookmark(user2.getId(), pin7.getId());
        bookmarkService.addBookmark(user2.getId(), pin8.getId());
        bookmarkService.addBookmark(user2.getId(), pin9.getId());
        bookmarkService.addBookmark(user1.getId(), pin9.getId());
        bookmarkService.addBookmark(user3.getId(), pin10.getId());
        bookmarkService.addBookmark(user3.getId(), pin11.getId());


        // 좋아요 등록
        // A(2), B(1), C(1), D(0), E(1)
        likesService.changeLikes(pinA.getId(), user1.getId(), true);
        likesService.changeLikes(pinA.getId(), user2.getId(), true);

        likesService.changeLikes(pinB.getId(), user1.getId(), true);
        likesService.changeLikes(pinB.getId(), user2.getId(), true);

        likesService.changeLikes(pinC.getId(), user1.getId(), true);
        likesService.changeLikes(pinC.getId(), user2.getId(), true);
        likesService.changeLikes(pinC.getId(), user1.getId(), false);   // 취소

        likesService.changeLikes(pinD.getId(), user1.getId(), true);
        likesService.changeLikes(pinD.getId(), user1.getId(), false);   // 취소

        likesService.changeLikes(pinE.getId(), user1.getId(), true);

        likesService.changeLikes(pin6.getId(), user1.getId(), true);
        likesService.changeLikes(pin6.getId(), user2.getId(), true);

        likesService.changeLikes(pin7.getId(), user1.getId(), true);

        likesService.changeLikes(pin8.getId(), user1.getId(), true);

        likesService.changeLikes(pin9.getId(), user1.getId(), true);

        likesService.changeLikes(pin10.getId(), user1.getId(), true);



        // 샘플 태그 등록
        Tag t1 = tagService.createTag("카페");
        Tag t2 = tagService.createTag("감성");
        Tag t3 = tagService.createTag("반려동물");
        Tag t4 = tagService.createTag("데이트");
        Tag t5 = tagService.createTag("야경");
        Tag t6 = tagService.createTag("산책로");
        Tag t7 = tagService.createTag("전망좋은");
        Tag t8 = tagService.createTag("최애식당");
        Tag t9 = tagService.createTag("지하철");

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

        PinTag pt13 = pinTagService.createPinTag(pin6, t8);
        PinTag pt14 = pinTagService.createPinTag(pin7, t9);
        PinTag pt15 = pinTagService.createPinTag(pin8, t5);  // 야경
        PinTag pt16 = pinTagService.createPinTag(pin9, t5);  // 야경
        PinTag pt17 = pinTagService.createPinTag(pin10, t5); // 야경
        PinTag pt18 = pinTagService.createPinTag(pin11, t7); // 전망좋은
        PinTag pt19 = pinTagService.createPinTag(pin11, t6); // 산책로
    }

}
