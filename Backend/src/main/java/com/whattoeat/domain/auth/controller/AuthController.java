package com.whattoeat.domain.auth.controller;

import com.whattoeat.domain.auth.dto.*;
import com.whattoeat.domain.auth.service.AuthService;
import com.whattoeat.global.exception.InvalidCredentialsException;
import com.whattoeat.global.rq.Rq;
import com.whattoeat.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final Rq rq;

    @PostMapping("/signup")
    public ResponseEntity<RsData<AuthUserResponse>> signup(@Valid @RequestBody SignUpRequest request) {
        AuthResult result = authService.signupAndLogin(request);
        rq.setCookie("accessToken", result.accessToken(), 60 * 60);
        rq.setCookie("refreshToken", result.refreshToken(), 60 * 60 * 24 * 7);
        return ResponseEntity.ok(RsData.success(result.userProfile(), "회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<RsData<AuthUserResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request);
        rq.setCookie("accessToken", result.accessToken(), 60 * 60);
        rq.setCookie("refreshToken", result.refreshToken(), 60 * 60 * 24 * 7);

        return ResponseEntity.ok(RsData.success(result.userProfile(), "로그인 성공"));
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<RsData<AuthUserResponse>> exchangeOAuthCode(@Valid @RequestBody OAuthExchangeRequest request) {
        AuthResult result = authService.exchangeOAuthCode(request.code());
        rq.setCookie("accessToken", result.accessToken(), 60 * 60);
        rq.setCookie("refreshToken", result.refreshToken(), 60 * 60 * 24 * 7);

        return ResponseEntity.ok(RsData.success(result.userProfile(), "로그인 성공"));
    }

    @PostMapping("/reissue")
    public ResponseEntity<RsData<Void>> reissue() {
        String refreshToken = rq.getCookieValue("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException("Refresh Token이 필요합니다.");
        }
        TokenResponse response = authService.reissue(refreshToken);

        rq.setCookie("accessToken", response.accessToken(), 60 * 60);
        rq.setCookie("refreshToken", response.refreshToken(), 60 * 60 * 24 * 7);

        return ResponseEntity.ok(RsData.success(null, "토큰이 갱신되었습니다."));
    }
    @PostMapping("/logout")
    public RsData<Void> logout(HttpServletRequest request) {
        String accessToken = rq.getCookieValue("accessToken");
        if (accessToken != null && !accessToken.isBlank()) {
            authService.logout(accessToken);
        }

        rq.delCookie("accessToken");
        rq.delCookie("refreshToken");

        return RsData.success(null, "로그아웃 되었습니다.");
    }
}
