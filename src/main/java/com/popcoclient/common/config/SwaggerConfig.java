package com.popcoclient.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
// 1. OpenAPI 기본 정보 설정
@OpenAPIDefinition(
        info = @Info(title = "Popco API", description = "Popco 서비스 API 명세서", version = "v1")
)
// 3. SecurityScheme 정의
@SecurityScheme(
        name = "bearerAuth", // SecurityRequirement에서 사용할 이름
        type = SecuritySchemeType.HTTP, // HTTP 방식
        scheme = "bearer", // Bearer 토큰 방식
        bearerFormat = "JWT" // 토큰 형식
)
public class SwaggerConfig {
}

