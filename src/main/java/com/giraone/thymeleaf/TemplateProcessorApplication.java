package com.giraone.thymeleaf;

import com.giraone.thymeleaf.config.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Locale;

@SpringBootApplication
public class TemplateProcessorApplication {

    private final Logger log = LoggerFactory.getLogger(TemplateProcessorApplication.class);

    private final ApplicationProperties applicationProperties;

    public TemplateProcessorApplication(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(TemplateProcessorApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startup() {
        // set default Locale to English, has to be changed dynamical when reports in other languages will be supported
        Locale.setDefault(Locale.ENGLISH);
        log.info("MainApplication READY");
        if (applicationProperties.isShowConfigOnStartup()) {
            log.info("{}", applicationProperties);
        }
    }
}
