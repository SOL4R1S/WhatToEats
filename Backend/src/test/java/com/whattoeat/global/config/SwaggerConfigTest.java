package com.whattoeat.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SwaggerConfig 테스트")
class SwaggerConfigTest {

    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        openAPI = new SwaggerConfig().openAPI();
    }

    @Test
    @DisplayName("API 제목과 버전이 올바르게 설정됨")
    void v1() {
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("WhatToEat API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
    }

    @Test
    @DisplayName("bearerAuth 보안 스킴이 JWT 타입으로 등록됨")
    void v2() {
        SecurityScheme securityScheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("모든 API에 bearerAuth 보안 요구사항이 적용됨")
    void v3() {
        assertThat(openAPI.getSecurity()).isNotEmpty();
        assertThat(openAPI.getSecurity().get(0).containsKey("bearerAuth")).isTrue();
    }
}
