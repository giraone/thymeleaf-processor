package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.service.JsonToHtmlProcessorUsingStringTemplates;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class RenderHtmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderHtmlController.class);

    private final JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor;

    @Autowired
    public RenderHtmlController(JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor) {
        this.jsonToHtmlProcessor = jsonToHtmlProcessor;
    }

    @Timed
    @PostMapping("/json-to-html")
    public void renderToHtmlByMultipartRequest(@RequestParam("data") MultipartFile dataFile,
                                               @RequestParam("template") MultipartFile templateFile,
                                               @RequestParam(value = "css", required = false) MultipartFile cssFile,
                                               HttpServletResponse response) throws IOException {

        LOGGER.warn("RenderHtmlController.renderToHtmlByMultipartRequest data={}, template={}, css={}", dataFile, templateFile, cssFile);
        byte[] dataBytes = dataFile.getBytes();
        byte[] htmlTemplateBytes = templateFile.getBytes();
        byte[] cssBytes = cssFile != null ? cssFile.getBytes() : null;

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        String error = jsonToHtmlProcessor.prepareTemplateWithContentAndData(
                out,
                new String(dataBytes, StandardCharsets.UTF_8),
                new String(htmlTemplateBytes, StandardCharsets.UTF_8),
                cssBytes != null ? new String(cssBytes, StandardCharsets.UTF_8) : null);

        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        if (error == null) {
            response.setStatus(HttpStatus.OK.value());
            try (OutputStream responseOut = response.getOutputStream()) {
                responseOut.write(out.toByteArray());
                responseOut.flush();
            }
        } else {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            try (OutputStream responseOut = response.getOutputStream()) {
                responseOut.write(error.getBytes(StandardCharsets.UTF_8));
                responseOut.flush();
            }
        }
    }
}