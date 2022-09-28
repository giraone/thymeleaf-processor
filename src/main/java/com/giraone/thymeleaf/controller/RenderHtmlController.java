package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.service.JsonToHtmlProcessorUsingStringTemplates;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.text.StringEscapeUtils;
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
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RenderHtmlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderHtmlController.class);

    public static final String PARAM_data = "data";
    public static final String PARAM_template = "template";
    public static final String PARAM_css = "css";

    private final JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor;

    @Autowired
    public RenderHtmlController(JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor) {
        this.jsonToHtmlProcessor = jsonToHtmlProcessor;
    }

    @Timed
    @Operation(
        summary = "Render an HTML report from a variable template, css file and variable JSON data.",
        description = "This endpoint is intended to be used ONLY from an editor (Redaktionssystem). " +
            "The file are passed in a multipart request.")
    @ApiResponse(responseCode = "200", description = "On successful generation of the HTML.")
    @PostMapping("/json-to-html")
    public void renderToHtmlByMultipartRequest(
        @RequestParam(PARAM_data) MultipartFile dataFile,
        @RequestParam(PARAM_template) MultipartFile templateFile,
        @RequestParam(value = PARAM_css, required = false) MultipartFile cssFile,
        HttpServletResponse response) throws IOException {

        LOGGER.warn("RenderHtmlController.renderToHtmlByMultipartRequest data={}, template={}, css={}",
            dataFile, templateFile, cssFile);
        byte[] dataBytes = dataFile.getBytes();
        byte[] htmlTemplateBytes = templateFile.getBytes();
        byte[] cssBytes = cssFile != null ? cssFile.getBytes() : null;

        final String dataString = new String(dataBytes, StandardCharsets.UTF_8);
        final Map<String, Object> dataMap;
        try {
            dataMap = JsonToHtmlProcessorUsingStringTemplates.convertDataJsonStringToMap(dataString);
        } catch (IOException e) {
            LOGGER.error("Failed to parse JSON data\r\n" + dataString, e);
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                "<hr /><h3>JSON Parsing Exception:</h3>"
                    + "<pre>" + StringEscapeUtils.escapeHtml4(e.getMessage()) + "</pre>"
                    + "<h3>JSON Data:</h3>"
                    + "<pre>" + dataString + "</pre>");
            return;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        String error = jsonToHtmlProcessor.prepareTemplateWithContentAndDataForHtmlOutput(
                out, dataMap,
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