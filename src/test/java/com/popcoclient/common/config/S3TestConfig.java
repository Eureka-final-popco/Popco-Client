package com.popcoclient.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class S3TestConfig {
    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }
}