package com.giraone.thymeleaf.config;

import com.giraone.thymeleaf.service.convert.Pd4mlConfiguration;
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

    /** Log the configuration to the log on startup */
    private boolean showConfigOnStartup;
    /** URL passed to JSON data as "base" while rendering HTML from JSON */
    private String htmlBase;
    /** URL passed to JSON data as "base" while rendering PDFs from JSON */
    private String htmlPdfBase;

    /** License configuration for PD4ML */
    private Pd4mlConfiguration pd4mlConfiguration = new Pd4mlConfiguration();

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

    public String getHtmlPdfBase() {
        return htmlPdfBase;
    }

    public void setHtmlPdfBase(String htmlPdfBase) {
        this.htmlPdfBase = htmlPdfBase;
    }

    public Pd4mlConfiguration getPd4mlConfiguration() {
        return pd4mlConfiguration;
    }

    public void setPd4mlConfiguration(Pd4mlConfiguration pd4mlConfiguration) {
        this.pd4mlConfiguration = pd4mlConfiguration;
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
            "showConfigOnStartup=" + showConfigOnStartup +
            ", htmlBase='" + htmlBase + '\'' +
            ", htmlPdfBase='" + htmlPdfBase + '\'' +
            ", pd4mlConfiguration=" + pd4mlConfiguration +
            '}';
    }
}
