package com.whattoeat.global.rq;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class RqTest {
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;
    private Rq rq;

    @BeforeEach
    void setUp() {
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        rq = new Rq(req, res);
    }

    @Test
    @DisplayName("쿠키 생성")
    void cookieCreate(){
        rq.setCookie("at","val");

        Cookie c = res.getCookie("at");
        assertThat(c).isNotNull();
        assertThat(c.getValue()).isEqualTo("val");
    }

    @Test
    @DisplayName("보안 설정")
    void setCookie_secure(){
        rq.setCookie("at","val");

        Cookie c = res.getCookie("at");
        assertThat(c.isHttpOnly()).isTrue();
        assertThat(c.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("쿠키 조회")
    void setCookie_get(){
        req.setCookies(new Cookie("at","val"));

        String v = rq.getCookieValue("at");

        assertThat(v).isEqualTo("val");

    }

    @Test
    @DisplayName("기본값조회")
    void getDefault(){
        String v = rq.getCookieValue("at","def");
        assertThat(v).isEqualTo("def");
    }

    @Test
    @DisplayName("쿠키 삭제")
    void delete(){
        rq.delCookie("at");

        Cookie c = res.getCookie("at");
        assertThat(c).isNotNull();

        //delCookie가 setCookie 호출하고 그 안에서 value.isBlank() ? 0 : MaxAge 처리하므로 getMaxAge 사용
        assertThat(c.getMaxAge()).isEqualTo(0);
    }
}
