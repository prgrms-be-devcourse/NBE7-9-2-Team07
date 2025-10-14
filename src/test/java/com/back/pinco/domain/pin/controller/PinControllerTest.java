package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.pin.repository.PinRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
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
    @DisplayName("특정 지점에서 범위 내 좌표 확인")
    void t1() throws Exception {
        long targetId = 1;
        Pin pin = pinRepository.findById(targetId).get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins")
                                .param("radius", String.valueOf(1))
                                .param("latitude", String.valueOf(pin.getLatitude()))
                                .param("longitude", String.valueOf(pin.getLongitude()))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.createDate").value(matchesPattern(pin.getCreateAt().toString().replaceAll("0+$", "") + ".*")))
                .andExpect(jsonPath("$.latitude").value(pin.getLatitude()))
                .andExpect(jsonPath("$.longitude").value(pin.getLongitude()));
    }

    @Test
    @DisplayName("모든 핀 리턴")
    void t2() throws Exception {


        ResultActions resultActions = mvc
                .perform(
                        get("/api/pins/all")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(PinController.class))
                .andExpect(handler().methodName("getAll"))
                .andExpect(status().isOk());
    }
}
