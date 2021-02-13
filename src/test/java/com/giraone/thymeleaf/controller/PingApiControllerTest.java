package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.TemplateProcessorApplication;
import com.giraone.thymeleaf.service.PingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PingApiResource REST controller.
 * Basically only to show, how these kind of MockMvc test without "full application context" (only web layer) work.
 *
 * @see PingApiController
 */
@SuppressWarnings("squid:S100") // Naming
@SpringBootTest(classes = { TemplateProcessorApplication.class })
@AutoConfigureMockMvc
class PingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PingService pingService;

    @DisplayName("Test GET /api/ping (web layer with mock)")
    @Test
    void assertThat_ping_works() throws Exception {

        when(pingService.getOkString()).thenReturn("OK");

        // act/assert
        mockMvc.perform(get("/api/ping")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE)) // Normally not necessary
            .andExpect(content().string(containsString("\"OK\""))) // Normally not necessary
            .andExpect(jsonPath("$.status").value("OK"));
    }
}
