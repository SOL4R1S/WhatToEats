package com.whattoeat.global.rq

import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RqTest {
    private lateinit var req: MockHttpServletRequest
    private lateinit var res: MockHttpServletResponse
    private lateinit var rq: Rq

    @BeforeEach
    fun setUp() {
        req = MockHttpServletRequest()
        res = MockHttpServletResponse()
        rq = Rq(req, res)
    }

    @Test
    @DisplayName("쿠키 생성")
    fun cookieCreate() {
        rq.setCookie("at", "val")

        val c = res.getCookie("at")
        assertThat(c).isNotNull()
        assertThat(c!!.value).isEqualTo("val")
    }

    @Test
    @DisplayName("보안 설정")
    fun setCookie_secure() {
        rq.setCookie("at", "val")

        val c = res.getCookie("at")
        assertThat(c).isNotNull()
        assertThat(c!!.isHttpOnly).isTrue()
        assertThat(c!!.path).isEqualTo("/")
    }

    @Test
    @DisplayName("쿠키 조회")
    fun setCookie_get() {
        req.setCookies(Cookie("at", "val"))

        val v = rq.getCookieValue("at")

        assertThat(v).isEqualTo("val")
    }

    @Test
    @DisplayName("기본값조회")
    fun getDefault() {
        val v = rq.getCookieValue("at", "def")

        assertThat(v).isEqualTo("def")
    }

    @Test
    @DisplayName("쿠키 삭제")
    fun delete() {
        rq.delCookie("at")

        val c = res.getCookie("at")
        assertThat(c).isNotNull()

        // delCookie가 setCookie 호출하고 그 안에서 value.isBlank() ? 0 : MaxAge 처리하므로 getMaxAge 사용
        assertThat(c!!.maxAge).isEqualTo(0)
    }
}