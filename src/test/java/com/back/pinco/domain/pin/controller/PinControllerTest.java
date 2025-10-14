package com.back.pinco.domain.pin.controller;

import com.back.pinco.domain.pin.repository.PinRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PinControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PinRepository pinRepository;

    @Test
    @DisplayName("")
    void t1(){}
}
