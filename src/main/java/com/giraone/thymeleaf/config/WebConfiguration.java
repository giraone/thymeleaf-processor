package com.giraone.thymeleaf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {

    private final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        log.info("WebConfiguration.addResourceHandlers");

        CacheControl cacheControl = CacheControl.maxAge(1L, TimeUnit.MINUTES); // TODO: add configuration

        registry.addResourceHandler("/favicon.ico")
            .addResourceLocations("classpath:static/favicon.ico")
            .setCacheControl(cacheControl);
        registry.addResourceHandler("/static/**")
            .addResourceLocations("classpath:static/")
            .setCacheControl(cacheControl);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        log.info("WebConfiguration.addViewController");
        registry.addRedirectViewController("/", "static/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*") // Allow CORS for every domain
            .allowedMethods("POST")
            .allowedHeaders("header1", "header2", "header3")
            .exposedHeaders("header1", "header2")
            // one hour
            .maxAge(3600);
        registry.addMapping("/static/**")
            .allowedOrigins("*") // Allow CORS for every domain
            .allowedMethods("GET")
            // one hour
            .maxAge(3600);
    }
}
