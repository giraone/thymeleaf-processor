package com.giraone.thymeleaf.controller;

import com.giraone.thymeleaf.service.JsonToHtmlProcessorUsingStringTemplates;
import com.giraone.thymeleaf.service.convert.HtmlToPdfConverter;
import com.giraone.thymeleaf.service.convert.PdfCreationOptions;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import static com.giraone.thymeleaf.controller.RenderHtmlController.*;

@RestController
@RequestMapping("/api")
public class RenderPdfController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderPdfController.class);

    private static final boolean USE_PDF_A = true;

    private static final String CONTENT_DISPOSITION_VALUE = "inline";

    private static final String DESCRIPTION_template = "Name of Thymeleaf template (located within resource folder) which is the base for the report";
    private static final String DESCRIPTION_css = "Name of CSS file (located within resource folder) which is used for styling the report";
    private static final String DESCRIPTION_jsonData = "JSON payload for Thymeleaf template";

    private final JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor;
    private final HtmlToPdfConverter htmlToPdfConverter;

    @Autowired
    public RenderPdfController(JsonToHtmlProcessorUsingStringTemplates jsonToHtmlProcessor,
                               HtmlToPdfConverter htmlToPdfConverter) {
        this.jsonToHtmlProcessor = jsonToHtmlProcessor;
        this.htmlToPdfConverter = htmlToPdfConverter;
    }

    @Timed
    @Operation(
        summary = "Render a PDF report from a variable template, css file and variable JSON data.",
        description = "This endpoint is intended to be used from an editor. " +
            "The file are passed in a multipart request.")
    @ApiResponse(responseCode = "200", description = "On successful generation of the PDF.")
    @PostMapping("/json-to-pdf")
    public void renderToPdfByMultipartRequest(
        @Parameter(description = DESCRIPTION_jsonData) @RequestParam(PARAM_data) MultipartFile dataFile,
        @Parameter(description = DESCRIPTION_template) @RequestParam(PARAM_template) MultipartFile templateFile,
        @Parameter(description = DESCRIPTION_css) @RequestParam(value = PARAM_css, required = false) MultipartFile cssFile,
        HttpServletResponse response) throws IOException {

        LOGGER.info("RenderPdfController.renderToPdfByMultipartRequest data={}, template={}, css={}",
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
        String error = jsonToHtmlProcessor.prepareTemplateWithContentAndDataForPdfOutput(
            out,
            dataMap,
            new String(htmlTemplateBytes, StandardCharsets.UTF_8),
            cssBytes != null ? new String(cssBytes, StandardCharsets.UTF_8) : null);

        if (error != null) {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            try (OutputStream responseOut = response.getOutputStream()) {
                responseOut.write(error.getBytes(StandardCharsets.UTF_8));
                responseOut.flush();
            }
        }

        String htmlString = out.toString(StandardCharsets.UTF_8);

        PdfCreationOptions pdfCreationOptions = new PdfCreationOptions();
        pdfCreationOptions.setPdfA(USE_PDF_A);
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) dataMap.get("metadata");
        if (metadata != null) {
            pdfCreationOptions.setDocumentTitle((String) metadata.get("title"));
            pdfCreationOptions.setDocumentAuthor((String) metadata.get("author"));
            pdfCreationOptions.setDocumentSubject((String) metadata.get("subject"));
            pdfCreationOptions.setDocumentKeywords((String) metadata.get("keywords"));
            pdfCreationOptions.setDocumentCreator((String) metadata.get("creator"));
        }

        pdfCreationOptions.setPdfA(USE_PDF_A);

        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, CONTENT_DISPOSITION_VALUE);

        final OutputStream pdfOutputStream = response.getOutputStream();
        htmlToPdfConverter.generatePdfStreamFromHtmlString(htmlString, pdfOutputStream, pdfCreationOptions);
        response.flushBuffer();
    }
}
