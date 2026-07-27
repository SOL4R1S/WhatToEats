package com.whattoeat.global.security;

import com.whattoeat.domain.auth.service.AuthService;
import com.whattoeat.domain.user.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;

    //application.yaml 파일에 app: fonrtend: url: 없으면 localhost:3000 동작
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("[OAuth2] onAuthenticationSuccess called");
        KakaoOAuth2User kakaoUser = (KakaoOAuth2User) authentication.getPrincipal();
        User user = kakaoUser.getUser();
        log.info("[OAuth2] authenticated user id={}", user.getId());

        // 여기서 쿠키를 바로 굽지 않는다: 이 응답은 프론트가 아니라 백엔드 자신의 도메인으로의
        // 직접 리다이렉트라서, 쿠키를 지금 세팅하면 백엔드 도메인에만 스코프된다.
        // 프론트가 /api 프록시로 이 코드를 교환할 때 쿠키를 세팅해야 프론트 도메인에 쿠키가 붙는다.
        String code = authService.createOAuthCode(user.getId());

        String redirectUri = frontendUrl;
        String stateParam = request.getParameter("state");
        log.info("[OAuth2] stateParam={}", stateParam);

        if (stateParam != null && !stateParam.isBlank()) {
            // Base64 URL-safe 디코딩
            String decodeState = new String(
                    Base64.getUrlDecoder().decode(stateParam),
                    StandardCharsets.UTF_8
            );
            redirectUri = decodeState.split("#", 2)[0];
        }
        redirectUri = redirectUri + "/oauth/callback?code=" + UriUtils.encode(code, StandardCharsets.UTF_8);
        log.info("[OAuth2] redirecting to {}", redirectUri);
        response.sendRedirect(redirectUri);
    }
}
