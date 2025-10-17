package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PinControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PinRepository pinRepository;

    @Test
    @DisplayName("핀 생성")
    void t1() throws Exception {

        double lat = 0;
        double lon = 0;
        String content = "new Content!";

        String jsonContent = String.format(
                """
                {
                    "content": "%s",
                    "latitude" : %s, 
                    "longitude" : %s
                }
                """, content, lat, lon
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/api/pins")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonContent)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("createPin"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.latitude").value(lat))
                .andExpect(jsonPath("$.data.longitude").value(lon))
                .andExpect(jsonPath("$.data.content").value(content));
    }

    @Test
    @DisplayName("id로 핀 조회 - 성공")
    void t2_1() throws Exception {
        long targetId = 302;
        Pin pin = pinRepository.findById(targetId).get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/%s".formatted(targetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pin.getId()))
                .andExpect(jsonPath("$.data.latitude").value(pin.getPoint().getY()))
                .andExpect(jsonPath("$.data.longitude").value(pin.getPoint().getX()))
                .andExpect(jsonPath("$.data.createAt").value(matchesPattern(pin.getCreatedAt().toString().replaceAll("0+$", "") + ".*")));
    }

    @Test
    @DisplayName("id로 핀 조회 - 실패")
    void t2_2() throws Exception {
        long targetId = Integer.MAX_VALUE;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/$s".formatted(targetId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getPinById"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("특정 지점에서 범위 내 좌표 확인")
    void t3_1() throws Exception {
        long targetId = 1;
        Pin pin = pinRepository.findById(targetId).get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins")
                                .param("radius", String.valueOf(1000))
                                .param("longitude", String.valueOf(pin.getPoint().getX()))
                                .param("latitude", String.valueOf(pin.getPoint().getY()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getRadiusPins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(1)))
                .andExpect(jsonPath("$.data[0].id").value(pin.getId()))
                .andExpect(jsonPath("$.data[0].latitude").value(pin.getPoint().getY()))
                .andExpect(jsonPath("$.data[0].longitude").value(pin.getPoint().getX()))
                .andExpect(jsonPath("$.data[0].createAt").value(matchesPattern(pin.getCreatedAt().toString().replaceAll("0+$", "") + ".*")));
    }

    @Test
    @DisplayName("특정 지점에서 범위 내 핀 확인 - 핀 없음")
    void t3_2() throws Exception {
        // 핀이 없는 위치와 반경을 설정합니다.
        double outOfRangeLat = 0;
        double outOfRangeLon = 0;
        double radius = 10; // 10미터

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins")
                                .param("radius", String.valueOf(radius))
                                .param("latitude", String.valueOf(outOfRangeLat))
                                .param("longitude", String.valueOf(outOfRangeLon))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getRadiusPins"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("모든 핀 리턴")
    void t4() throws Exception {
        List<Pin> pins = pinRepository.findAll();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/all")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()", is(pins.size())));

        for (int i = 0; i < pins.size(); i++) {
            Pin pin = pins.get(i);
            resultActions
                    .andExpect(jsonPath("$.data[%d].id".formatted(i)).value(pin.getId()))
                    .andExpect(jsonPath("$.data[%d].latitude".formatted(i)).value(pin.getPoint().getY()))
                    .andExpect(jsonPath("$.data[%d].longitude".formatted(i)).value(pin.getPoint().getX()))
                    .andExpect(jsonPath("$.data[%d].createAt".formatted(i)).value(matchesPattern(pin.getCreatedAt().toString().replaceAll("0+$", "") + ".*")));
        }
    }

    @Test
    @DisplayName("핀 내용 수정")
    void t5_1() throws Exception {
        Long pinId = 1L;
        String content = "updated Content!";
        Pin pin = pinRepository.findById(pinId).get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/$f".formatted(pinId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "content": "%s"
                                        }
                                        """.formatted(content))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("updatePinContent"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").value(pinId))
                .andExpect(jsonPath("$.data.latitude").value(pin.getPoint().getY()))
                .andExpect(jsonPath("$.data.longitude").value(pin.getPoint().getX()))
                .andExpect(jsonPath("$.data.content").value(content));
    }


    @Test
    @DisplayName("핀 공개 여부 수정")
    void t5_2() throws Exception {
        Long pinId = 1L;
        String content = "updated Content!";
        Pin pin = pinRepository.findById(pinId).get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/pins/$f/public".formatted(pinId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("updatePinContent"))
                .andExpect(status().isOk());

        resultActions
                .andExpect(jsonPath("$.data.id").value(pinId))
                .andExpect(jsonPath("$.data.isPublic").value(!pin.getIsPublic()));
    }

    @Test
    @DisplayName("핀 삭제 - 성공")
    void t6() throws Exception {
        Long pinId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/pins/$f".formatted(pinId))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("deletePin"))
                .andExpect(status().isOk());
    }
}
