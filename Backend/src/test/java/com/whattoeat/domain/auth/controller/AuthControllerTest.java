package com.whattoeat.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattoeat.domain.auth.dto.*;
import com.whattoeat.domain.auth.service.AuthService;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.global.exception.DuplicateLoginIdException;
import com.whattoeat.global.exception.DuplicateNicknameException;
import com.whattoeat.global.exception.InvalidCredentialsException;
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.rq.Rq;
import com.whattoeat.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;


@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.whattoeat.global.jwt.JwtAuthenticationFilter.class,
                        com.whattoeat.global.config.SecurityConfig.class
                }))
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(c -> c.disable())
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private Rq rq;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    // ========== POST /api/v1/auth/signup ==========

    @Test
    @DisplayName("정상 입력으로 회원가입 성공 시 200 반환 및 쿠키 설정")
    void signupSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest("test@tset.com", "pass1234", "testnick");
        AuthUserResponse userProfile = new AuthUserResponse(
                1L, "testnick", null, "test@test.com", Provider.LOCAL, Role.USER, LocalDateTime.now()
        );
        AuthResult result = new AuthResult("mocked-access-token", "mocked-refresh-token", userProfile);
        given(authService.signupAndLogin(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("testnick"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        then(rq).should().setCookie("accessToken", "mocked-access-token", 60 * 60);
        then(rq).should().setCookie("refreshToken", "mocked-refresh-token", 60 * 60 * 24 * 7);
    }

    @Test
    @DisplayName("아이디 중복 시 409 반환")
    void signupFailDuplicateLoginId() throws Exception {
        SignUpRequest request = new SignUpRequest("test@tset.com", "pass1234", "testnick");
        willThrow(new DuplicateLoginIdException("이미 사용 중인 아이디입니다.")).given(authService).signupAndLogin(any());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
    }

    @Test
    @DisplayName("닉네임 중복 시 409 반환")
    void signupFailDuplicateNickname() throws Exception {
        SignUpRequest request = new SignUpRequest("test@tset.com", "pass1234", "testnick");
        willThrow(new DuplicateNicknameException("이미 사용 중인 닉네임입니다.")).given(authService).signupAndLogin(any());

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("아이디 빈 값으로 회원가입 시 400 반환")
    void signupFailBlankLoginId() throws Exception {
        SignUpRequest request = new SignUpRequest("", "pass1234", "testnick");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("loginId")));
    }

    @Test
    @DisplayName("비밀번호 4자 미만으로 회원가입 시 400 반환")
    void signupFailShortPassword() throws Exception {
        SignUpRequest request = new SignUpRequest("test@tset.com", "abc", "testnick");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("password")));
    }

    @Test
    @DisplayName("이메일 형식이 아닌 값으로 회원가입 시 400 반환")
    void signupFailInvalidEmail() throws Exception {
        SignUpRequest request = new SignUpRequest("invalid-email", "pass1234", "testnick");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("loginId")));
    }

    // ========== POST /api/v1/auth/login ==========

    @Test
    @DisplayName("정상 아이디/비밀번호로 로그인 성공 시 200과 토큰 반환")
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("test@tset.com", "pass1234");
        AuthUserResponse userProfile = new AuthUserResponse(
                1L, "testnick", null, "test@test.com", Provider.LOCAL,Role.USER, LocalDateTime.now()
        );
        AuthResult result = new AuthResult("mocked-access-token", "mocked-refresh-token", userProfile);
        given(authService.login(any())).willReturn(result);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("testnick"))
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.provider").value("LOCAL"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.message").value("로그인 성공"));

        then(rq).should().setCookie("accessToken", "mocked-access-token", 60 * 60);
        then(rq).should().setCookie("refreshToken", "mocked-refresh-token", 60 * 60 * 24 * 7);
    }

    @Test
    @DisplayName("아이디 또는 비밀번호 불일치 시 401 반환")
    void loginFailInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@tset.com", "wrongpass");
        given(authService.login(any())).willThrow(new InvalidCredentialsException("아이디/비밀번호가 올바르지 않습니다."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디/비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("아이디 빈 값으로 로그인 시 400 반환")
    void loginFailBlankLoginId() throws Exception {
        LoginRequest request = new LoginRequest("", "pass1234");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("loginId")));
    }

    // ========== POST /api/v1/auth/reissue ==========

    @Test
    @DisplayName("유효 refreshToken으로 재발급시 200 반환")
    void refreshTokenSuccess() throws Exception {
        given(rq.getCookieValue("refreshToken")).willReturn("valid-token");
        TokenResponse res = new TokenResponse("new-access-token", "new-refresh-token");
        given(authService.reissue("valid-token")).willReturn(res);

        mockMvc.perform(post("/api/v1/auth/reissue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다."));

        then(rq).should().setCookie("accessToken", "new-access-token", 60 * 60);
        then(rq).should().setCookie("refreshToken", "new-refresh-token", 60 * 60 * 24 * 7);
    }

    @Test
    @DisplayName("refreshToken 없이 재발급 요청 시 401 반환")
    void refreshTokenMissing() throws Exception {
        given(rq.getCookieValue("refreshToken")).willReturn(null);

        mockMvc.perform(post("/api/v1/auth/reissue"))
                .andExpect(status().isUnauthorized());
    }

    // ========== POST /api/v1/auth/logout ==========

    @Test
    @DisplayName("accessToken쿠키와 함께 로그아웃 요청 시 200 반환")
    void logoutSuccess() throws Exception {
        given(rq.getCookieValue("accessToken")).willReturn("valid-token");
        willDoNothing().given(authService).logout("valid-token");

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."));

        then(authService).should().logout("valid-token");
        then(rq).should().delCookie("accessToken");
        then(rq).should().delCookie("refreshToken");
    }

    @Test
    @DisplayName("accessToken쿠키 없이 로그아웃 요청 시 서비스 호출 없이 200 반환")
    void logoutWithoutToken() throws Exception {
        given(rq.getCookieValue("accessToken")).willReturn(null);

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
