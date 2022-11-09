package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.common.FileUtil;
import com.giraone.thymeleaf.common.assertions.pdf.PdfContentAssertion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"squid:S100"}) // Naming, Duplicate code
@SpringBootTest(webEnvironment = DEFINED_PORT)
@AutoConfigureMockMvc
class RenderPdfControllerIntTest {

    private static final String SRC_INPUT = "testdata/input/";

    private final MockMvc mockMvc;

    @Autowired
    public RenderPdfControllerIntTest(MockMvc mockMvc) {

        this.mockMvc = mockMvc;

        Path parentDir = Paths.get("target/test/output");
        if (!Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException ioe) {
                throw new IllegalStateException("Cannot create target/test/output!", ioe);
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
        "simple/input,simple/input,simple/input,false,simple",
        "persons-table/persons,persons-table/persons,persons-table/persons,false,persons-table",
        "using-fonts/input,using-fonts/input,using-fonts/input,true,using-fonts"
    })
    void assertThat_renderToPdfByMultipartRequest_basicallyWorks(
        String jsonName,
        String htmlName,
        String cssName,
        boolean checkPdfA,
        String outputName
    ) throws Exception {

        // arrange
        String jsonData = FileUtil.readTextFileFromResource(SRC_INPUT + jsonName + ".json", Charset.defaultCharset());
        assertThat(jsonData).isNotNull();
        String template = FileUtil.readTextFileFromResource(SRC_INPUT + htmlName + ".html", Charset.defaultCharset());
        assertThat(template).isNotNull();
        String css = FileUtil.readTextFileFromResource(SRC_INPUT + cssName + ".css", Charset.defaultCharset());
        assertThat(css).isNotNull();

        MockMultipartFile mpData = new MockMultipartFile("data", "file.json",
            "application/json", jsonData.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile mpTemplate = new MockMultipartFile("template", "file.html",
            "text/html", template.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile mpCss = new MockMultipartFile("css", "file.css",
            "text/css", css.getBytes(StandardCharsets.UTF_8));

        MockMultipartHttpServletRequestBuilder mockMultipartHttpServletRequestBuilder =
            MockMvcRequestBuilders.multipart("/api/json-to-pdf")
                .file(mpData)
                .file(mpTemplate)
                .file(mpCss);

        // act/assert
        byte[] pdfBytes = mockMvc.perform(mockMultipartHttpServletRequestBuilder)
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"))
            .andReturn().getResponse().getContentAsByteArray();

        // Write PDF content for troubleshooting
        File outputFile = new File("target/test/output/json-to-pdf-" + outputName + ".pdf");
        Files.write(outputFile.toPath(), pdfBytes);

        // assert
        PdfContentAssertion.assertThat(pdfBytes).isValidPdf();

        if (checkPdfA) {
            PdfContentAssertion.assertThat(pdfBytes).isValidPdfA();
        }
        // Delete, if everything was OK
        outputFile.delete();
    }
}
