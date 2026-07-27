package com.whattoeat.global.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.token";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보 저장")
    void v1() throws Exception {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.USER);
        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        Claims claims = mock(Claims.class);
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims);
        given(claims.get("tokenType", String.class)).willReturn("access");
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L);
        given(customUserDetailsService.loadUserByUsername("1")).willReturn(customUserDetails);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);

        jwtAuthenticationFilter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("위변조된 토큰이면 인증 정보 없이 다음 필터로 진행 (permitAll 엔드포인트를 막지 않기 위함)")
    void v2() throws Exception {
        given(jwtUtil.parseToken(INVALID_TOKEN)).willThrow(new JwtException("invalid token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + INVALID_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("토큰이 없으면 SecurityContext에 인증 정보 없음")
    void v3() throws Exception {
        jwtAuthenticationFilter.doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("블랙리스트에 있는 토큰으로 요청 시 인증 정보 없이 다음 필터로 진행")
    void blacklistedToken_skipsAuthentication() throws Exception {
        Claims claims = mock(Claims.class);
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims);
        given(claims.get("tokenType", String.class)).willReturn("access");
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L);
        given(redisTemplate.hasKey("blacklist:" + VALID_TOKEN)).willReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("accessToken 쿠키로 인증")
    void v4() throws Exception {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.USER);
        CustomUserDetails customUserDetails = new CustomUserDetails(mockUser);

        Claims claims = mock(Claims.class);
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims);
        given(claims.get("tokenType", String.class)).willReturn("access");
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L);
        given(customUserDetailsService.loadUserByUsername("1")).willReturn(customUserDetails);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("accessToken", VALID_TOKEN));

        jwtAuthenticationFilter.doFilter(req, new MockHttpServletResponse(), mock(FilterChain.class));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
    }
}
