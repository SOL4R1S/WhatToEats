package com.whattoeat.global.jwt;

import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@DisplayName("JwtUtil 테스트")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "dGVzdFNlY3JldEtleUZvckp3dFRlc3RpbmdQdXJwb3Nl";
    private static final long TEST_ACCESS_EXPIRATION = 900000L;   // 15분
    private static final long TEST_REFRESH_EXPIRATION = 604800000L;     // 7일


    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", TEST_ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", TEST_REFRESH_EXPIRATION);
        jwtUtil.init();

        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getLoginId()).thenReturn("testUser");
        when(mockUser.getRole()).thenReturn(Role.USER);
    }

    @Test
    @DisplayName("유저 정보로 access token 생성")
    void generateAccessToken() {
        String token = jwtUtil.generateAccessToken(mockUser);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("유저 정보로 refresh token 생성")
    void generateRefreshToken() {
        String token = jwtUtil.generateRefreshToken(mockUser);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }


    @Test
    @DisplayName("유효한 토큰을 파싱하면 예외 없이 Claims를 반환")
    void parseToken_valid() {
        String token = jwtUtil.generateAccessToken(mockUser);

        assertThatCode(() -> jwtUtil.parseToken(token)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("만료된 토큰을 파싱하면 ExpiredJwtException 발생")
    void parseToken_expired() {
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", -1L);
        String expiredToken = jwtUtil.generateAccessToken(mockUser);

        assertThatThrownBy(() -> jwtUtil.parseToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("잘못된 형식의 토큰을 파싱하면 JwtException 발생")
    void parseToken_invalid() {
        assertThatThrownBy(() -> jwtUtil.parseToken("invalid.token.value"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("토큰에서 userId를 추출하면 토큰 생성 시 넣은 userId와 동일")
    void getUserId() {
        String token = jwtUtil.generateAccessToken(mockUser);

        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("토큰에서 role을 추출하면 토큰 생성 시 넣은 role과 동일")
    void getRole() {
        String token = jwtUtil.generateAccessToken(mockUser);

        assertThat(jwtUtil.getRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("refreshToken에서 userId 추출 시 accessToken과 동일")
    void getUserIdFromRefreshToken() {
        String accessToken = jwtUtil.generateAccessToken(mockUser);
        String refreshToken = jwtUtil.generateRefreshToken(mockUser);
        assertThat(jwtUtil.getUserId(refreshToken)).isEqualTo(jwtUtil.getUserId(accessToken));
    }

    @Test
    @DisplayName("refreshToken 만료 시 ExpiredJwtException 발생")
    void parseRefreshToken_expired() {
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", -1L);
        String expiredToken = jwtUtil.generateRefreshToken(mockUser);

        assertThatThrownBy(() -> jwtUtil.parseToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
