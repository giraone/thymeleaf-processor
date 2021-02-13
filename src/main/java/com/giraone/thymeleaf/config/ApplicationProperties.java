package com.giraone.thymeleaf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties specific to application.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)

public class ApplicationProperties {

    private boolean showConfigOnStartup;

    private String htmlBase;

    public boolean isShowConfigOnStartup() {
        return showConfigOnStartup;
    }

    public void setShowConfigOnStartup(boolean showConfigOnStartup) {
        this.showConfigOnStartup = showConfigOnStartup;
    }

    public String getHtmlBase() {
        return htmlBase;
    }

    public void setHtmlBase(String htmlBase) {
        this.htmlBase = htmlBase;
    }
}
