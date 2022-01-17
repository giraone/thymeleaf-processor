package com.giraone.thymeleaf.service;

import com.giraone.thymeleaf.common.JsonUtil;
import com.giraone.thymeleaf.config.ApplicationProperties;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class JsonToHtmlProcessorUsingStringTemplates {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonToHtmlProcessorUsingStringTemplates.class);
    private static final long ONE_HOUR = 3_600_000L;

    private final ApplicationProperties applicationProperties;
    private SpringTemplateEngine templateEngine;

    @Autowired
    public JsonToHtmlProcessorUsingStringTemplates(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private static Map<String, Object> convertDataJsonStringToMap(String dataJsonString) throws IOException {
        final Map<String, Object> data;
        if (dataJsonString.trim().startsWith("[")) {
            List<Map<String, Object>> listOfMaps = JsonUtil.convertToJsonList(dataJsonString);
            data = Map.of("list", listOfMaps);
        } else {
            data = JsonUtil.convertToJsonMap(dataJsonString);
        }
        return data;
    }

    @PostConstruct
    private void init() {

        final StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheTTLMs(ONE_HOUR);
        templateEngine = new SpringTemplateEngine();
        templateEngine.addDialect(new Java8TimeDialect()); // to use #dates object
        templateEngine.setTemplateResolver(resolver);
        // templateEngine.setLinkBuilder(new CustomLinkBuilder());
        LOGGER.info("SpringTemplateEngine using StringTemplateResolver for JsonToHtmlProcessor initialized");
    }


    //------------------------------------------------------------------------------------------------------------------

    public String prepareTemplateWithContentAndDataForPdfOutput(
        OutputStream out, String jsonData, String templateContent, String cssContent) throws IOException {
        return prepareTemplateWithContentAndData(out, jsonData, templateContent, cssContent, applicationProperties.getHtmlPdfBase());
    }

    public String prepareTemplateWithContentAndDataForHtmlOutput(
        OutputStream out, String jsonData, String templateContent, String cssContent) throws IOException {
        return prepareTemplateWithContentAndData(out, jsonData, templateContent, cssContent, applicationProperties.getHtmlBase() );
    }

    public String prepareTemplateWithContentAndData(
        OutputStream out, String jsonData, String templateContent, String cssContent, String basePath) throws IOException {

        final Map<String, Object> data;
        try {
            data = convertDataJsonStringToMap(jsonData);
        } catch (IOException e) {
            LOGGER.error("Failed to parse JSON data\r\n" + jsonData, e);
            return "<hr /><h3>JSON Parsing Exception:</h3>"
                + "<pre>" + StringEscapeUtils.escapeHtml4(e.getMessage()) + "</pre>"
                + "<h3>JSON Data:</h3>"
                + "<pre>" + jsonData + "</pre>";
        }

        if (cssContent != null) {
            templateContent = templateContent.replace(
                "<style></style>",
                "<style>" + cssContent + "</style>");
        }

        final HashMap<String, Object> staticData = new HashMap<>();
        if (basePath != null && basePath.trim().length() > 0) {
            staticData.put("base", basePath.trim());
        } else {
            staticData.put("base", null);
        }
        staticData.put("css", null);
        data.put("_static", staticData);

        final Context context = new Context(Locale.GERMAN, data);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(out))) {
            try {
                templateEngine.process(templateContent, context, writer);
                return null;

            } catch (TemplateInputException templateInputException) {
                LOGGER.error("Failed to process\r\n" + templateContent + "\r\n" + data, templateInputException);
                return "<hr /><h3>Template Input Exception:</h3>"
                    + "<pre>" + StringEscapeUtils.escapeHtml4(templateInputException.getCause().getMessage()) + "</pre>"
                    + "<h3>JSON Data:</h3>"
                    + "<pre>" + JsonUtil.convertObjectToJsonString(data) + "</pre>";
            } catch (Exception exception) {
                LOGGER.error("Failed to process\r\n" + templateContent + "\r\n" + data, exception);

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                exception.printStackTrace(printWriter);

                return "<hr /><h3>Exception:</h3>"
                    + "<pre>" + StringEscapeUtils.escapeHtml4(exception.getMessage()) + "</pre>"
                    + "<h3>JSON Data:</h3>"
                    + "<pre>" + JsonUtil.convertObjectToJsonString(data) + "</pre>"
                    + "<h3>Stack Trace:</h3>"
                    + "<pre>" + StringEscapeUtils.escapeHtml4(stringWriter.toString()) + "</pre>";
            }
        }
    }
}
