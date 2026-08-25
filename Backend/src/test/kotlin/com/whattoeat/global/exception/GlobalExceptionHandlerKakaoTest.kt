package com.whattoeat.global.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GlobalExceptionHandlerKakaoTest {

    private val handler = GlobalExceptionHandler()

    @Test
    @DisplayName("KakaoApiException 발생 시 502 상태코드 반환")
    fun handleKakaoApiException_returns502() {
        val exception = KakaoApiException("카카오 API 서버 오류")

        val response = handler.handleKakaoApiException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_GATEWAY)
    }

    @Test
    @DisplayName("KakaoApiException 발생 시 응답 바디에 메시지 포함")
    fun handleKakaoApiException_containsMessage() {
        val exception = KakaoApiException("카카오 API 요청 오류 :401 UNAUTHORIZED")

        val response = handler.handleKakaoApiException(exception)

        assertThat(response.body).isNotNull()
        assertThat(response.body!!.message).isEqualTo("카카오 API 요청 오류 :401 UNAUTHORIZED")
        assertThat(response.body!!.status).isEqualTo(502)
    }
}