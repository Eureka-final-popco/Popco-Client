package com.popcoclient.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${fastapi.url}")
    private String fastapiBaseUrl;

    @PostConstruct
    public void init() {
        log.info("FastAPI Base URL from properties (via SLF4J logger): {}", fastapiBaseUrl);
        System.out.println(">>> FastAPI Base URL from properties (via System.out.println): " + fastapiBaseUrl);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        if (fastapiBaseUrl == null || fastapiBaseUrl.isEmpty()) {
            log.error("fastapi.url is not configured! WebClient will be built without a base URL.");
            return builder.build();
        }
        return builder
                .baseUrl(fastapiBaseUrl)
                .build();
    }
}