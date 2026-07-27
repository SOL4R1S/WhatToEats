package com.whattoeat.global.rq;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    // 배포시(https사용)엔 application.yaml에서 cookie: secure: true 변경 필요
    @Value("${cookie.secure:false}")
    private boolean secure;

    // 배포시엔 application.yaml에서 cookie: same-site: Strict, Lax , None 선택 필요
    // 프론트/백이 같은 도메인이면 strict, Lax. 프론트/백이 완전 다르면 None 선택
    @Value("${cookie.same-site:Lax}")
    private String cookieSameSite;

    public String getCookieValue(String name, String defaultValue) {
        return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst().orElse(defaultValue);
    }

    public String getCookieValue(String name) {
        return getCookieValue(name, null);
    }

    public void setCookie(String name, String value, int maxAge) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(value.isBlank() ? 0 : maxAge);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
    }

    public void setCookie(String name, String value) {
        setCookie(name, value, 60*60*24);
    }

    public void delCookie(String name) {
        setCookie(name, null);
    }

    public void setReadableCookie(String name, String value, int maxAge) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(false);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(value.isBlank() ? 0 : maxAge);
        cookie.setAttribute("SameSite", cookieSameSite);
        response.addCookie(cookie);
    }

}
