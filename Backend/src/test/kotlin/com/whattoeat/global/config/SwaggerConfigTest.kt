package com.whattoeat.global.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SwaggerConfig 테스트")
class SwaggerConfigTest {

    private lateinit var openAPI: OpenAPI

    @BeforeEach
    fun setUp() {
        openAPI = SwaggerConfig().openAPI()
    }

    @Test
    @DisplayName("API 제목과 버전이 올바르게 설정됨")
    fun `api metadata는 올바르다`() {
        assertThat(openAPI.info.title).isEqualTo("WhatToEat API")
        assertThat(openAPI.info.version).isEqualTo("v1")
    }

    @Test
    @DisplayName("bearerAuth 보안 스킴이 JWT 타입으로 등록됨")
    fun `bearerAuth 스킴은 JWT 타입이다`() {
        val securityScheme = openAPI.components.securitySchemes["bearerAuth"]

        assertThat(securityScheme).isNotNull()
        assertThat(securityScheme!!.type).isEqualTo(SecurityScheme.Type.HTTP)
        assertThat(securityScheme.scheme).isEqualTo("bearer")
        assertThat(securityScheme.bearerFormat).isEqualTo("JWT")
    }

    @Test
    @DisplayName("모든 API에 bearerAuth 보안 요구사항이 적용됨")
    fun `전체 보안 요구사항에 bearerAuth가 존재한다`() {
        assertThat(openAPI.security).isNotEmpty
        assertThat(openAPI.security[0]).containsKey("bearerAuth")
    }
}