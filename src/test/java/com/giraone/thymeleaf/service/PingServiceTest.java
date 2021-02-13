package com.giraone.thymeleaf.service;

import com.giraone.thymeleaf.TemplateProcessorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("squid:S100") // Naming
@SpringBootTest(classes = TemplateProcessorApplication.class)
class PingServiceTest {

    @Autowired
    PingService pingService;

    @Test
    void assertThat_getString_works() {

        assertThat(pingService.getOkString()).isEqualTo("OK");
    }
}