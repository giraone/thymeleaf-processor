package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.TemplateProcessorApplication;
import com.giraone.thymeleaf.common.FileUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TemplateProcessorApplication.class)
@AutoConfigureMockMvc
public class RenderHtmlControllerIntTest {

    private static final String SRC_INPUT = "/testdata/input";
    private static final String TARGET_OUTPUT = "target/test/output";
    private static final String URL ="/api/json-to-html";

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderHtmlControllerIntTest.class);

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void init() {
        if (!new File(TARGET_OUTPUT).mkdirs()) {
            LOGGER.error("Cannot create output directory \"{}\"!", TARGET_OUTPUT);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "{\"something\":\"GOOD\"},                 <div th:text=\"${something}\">BAD</div>,            <div>GOOD</div>",
        "{\"anything\":{\"something\":\"GOOD\"}},  <div th:text=\"${anything.something}\">BAD</div>,   <div>GOOD</div>",
        "{\"something\":\"GOOD\"},                 <a th:href=\"${something}\">,                       <a href=\"GOOD\">"
    })
    public void renderToHtmlByMultipartRequest_UsingTinyHtmlAndNoCss(String dataJson, String template, String expectedHtml) throws Exception {

        MockMultipartFile dataFile = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes());
        MockMultipartFile templateFile = new MockMultipartFile("template", "template", MediaType.TEXT_HTML_VALUE, template.getBytes());

        // act
        MvcResult res = mockMvc.perform(
            multipart(URL)
                .file(dataFile)
                .file(templateFile)
                .characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

        // assert
        String contentAsString = res.getResponse().getContentAsString();
        contentAsString = alignPlatformSpecificProperties(contentAsString);
        assertThat(contentAsString).isEqualTo(expectedHtml);
    }

    //------------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
        "{\"s\":\"S\"},  <head><title th:text=\"${s}\">T</title><style></style></head><div th:text=\"${s}\">T</div>,  { div { color: red } },  <head><title>S</title><style>{ div { color: red } }</style></head><div>S</div>"
    })
    public void renderToHtmlByMultipartRequest_UsingTinyHtmlAndCss(String dataJson, String template, String css, String expectedHtml) throws Exception {

        MockMultipartFile dataFile = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes());
        MockMultipartFile templateFile = new MockMultipartFile("template", "template", MediaType.TEXT_HTML_VALUE, template.getBytes());
        MockMultipartFile cssFile = new MockMultipartFile("css", "css", "text/css", css.getBytes());

        // act
        MvcResult res = mockMvc.perform(
            multipart(URL)
                .file(dataFile)
                .file(templateFile)
                .file(cssFile)
                .characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

        // assert
        String contentAsString = res.getResponse().getContentAsString();
        contentAsString = alignPlatformSpecificProperties(contentAsString);
        assertThat(contentAsString).isEqualTo(expectedHtml);
    }

    @Test
    public void renderToHtmlByMultipartRequest_UsingTestFiles() throws Exception {

        // arrange
        String dataJson = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple/input.json");
        assertThat(dataJson).isNotNull();
        String template = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple/input.html");
        assertThat(template).isNotNull();
        String css = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple/input.css");
        assertThat(css).isNotNull();

        String expectedHtml = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple/output.html");
        assertThat(expectedHtml).isNotNull();
        expectedHtml = alignPlatformSpecificProperties(expectedHtml);

        MockMultipartFile dataFile = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes());
        MockMultipartFile templateFile = new MockMultipartFile("template", "template", MediaType.TEXT_HTML_VALUE, template.getBytes());
        MockMultipartFile cssFile = new MockMultipartFile("css", "css", "text/css", css.getBytes());

        // act
        MvcResult res = mockMvc.perform(
            multipart(URL)
                .file(dataFile)
                .file(templateFile)
                .file(cssFile)
                .characterEncoding("UTF-8"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

        // assert
        String contentAsString = res.getResponse().getContentAsString();
        contentAsString = alignPlatformSpecificProperties(contentAsString);
        assertThat(contentAsString).isEqualTo(expectedHtml);
        Files.write(Paths.get(TARGET_OUTPUT + "/template-simple-put.html"), contentAsString.getBytes());
    }

    @Test
    public void renderToHtmlByMultipartRequest_WithError() throws Exception {

        // arrange
        String dataJson = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple-parser-error/input.json");
        assertThat(dataJson).isNotNull();
        String template = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple-parser-error/input.html");
        assertThat(template).isNotNull();
        String css = FileUtil.readTextFileFromResource(SRC_INPUT + "/simple-parser-error/input.css");
        assertThat(css).isNotNull();

        MockMultipartFile dataFile = new MockMultipartFile("data", "data", MediaType.APPLICATION_JSON_VALUE, dataJson.getBytes());
        MockMultipartFile templateFile = new MockMultipartFile("template", "template", MediaType.TEXT_HTML_VALUE, template.getBytes());
        MockMultipartFile cssFile = new MockMultipartFile("css", "css", "text/css", css.getBytes());

        // act
        MvcResult res = mockMvc.perform(
            multipart(URL)
                .file(dataFile)
                .file(templateFile)
                .file(cssFile)
                .characterEncoding("UTF-8"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andReturn();

        // assert
        String contentAsString = res.getResponse().getContentAsString();
        contentAsString = alignPlatformSpecificProperties(contentAsString);
        assertThat(contentAsString).contains("An error happened during template parsing");
    }

    //------------------------------------------------------------------------------------------------------------------

    private static String alignPlatformSpecificProperties(String content) {
        return normalizeEolChars(content);
    }

    private static String normalizeEolChars(String fileContent) {
        fileContent = fileContent.replaceAll("\\r\\n", "\n");
        return fileContent.replaceAll("\\r", "\n");
    }
}
