package com.whattoeat.global.jwt

import com.whattoeat.domain.user.entity.Role
import com.whattoeat.domain.user.entity.User
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.test.util.ReflectionTestUtils

@DisplayName("JwtUtil 테스트")
class JwtUtilTest {

    private lateinit var jwtUtil: JwtUtil

    private val mockUser: User = mock(User::class.java)

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil()
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET)
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", TEST_ACCESS_EXPIRATION)
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", TEST_REFRESH_EXPIRATION)
        jwtUtil.init()

        // Kotlin 프로퍼티 접근을 통해 getter 스텁
        `when`(mockUser.id).thenReturn(1L)
        `when`(mockUser.loginId).thenReturn("testUser")
        `when`(mockUser.role).thenReturn(Role.USER)
    }

    @Test
    @DisplayName("유저 정보로 access token 생성")
    fun generateAccessToken() {
        val token = jwtUtil.generateAccessToken(mockUser)

        assertThat(token).isNotNull()
        assertThat(token.split(".")).hasSize(3)
    }

    @Test
    @DisplayName("유저 정보로 refresh token 생성")
    fun generateRefreshToken() {
        val token = jwtUtil.generateRefreshToken(mockUser)

        assertThat(token).isNotNull()
        assertThat(token.split(".")).hasSize(3)
    }

    @Test
    @DisplayName("유효한 토큰을 파싱하면 예외 없이 Claims를 반환")
    fun parseToken_valid() {
        val token = jwtUtil.generateAccessToken(mockUser)

        assertThatCode { jwtUtil.parseToken(token) }.doesNotThrowAnyException()
    }

    @Test
    @DisplayName("만료된 토큰을 파싱하면 ExpiredJwtException 발생")
    fun parseToken_expired() {
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", -1L)
        val expiredToken = jwtUtil.generateAccessToken(mockUser)

        assertThatThrownBy { jwtUtil.parseToken(expiredToken) }
            .isInstanceOf(ExpiredJwtException::class.java)
    }

    @Test
    @DisplayName("잘못된 형식의 토큰을 파싱하면 JwtException 발생")
    fun parseToken_invalid() {
        assertThatThrownBy { jwtUtil.parseToken("invalid.token.value") }
            .isInstanceOf(JwtException::class.java)
    }

    @Test
    @DisplayName("토큰에서 userId를 추출하면 토큰 생성 시 넣은 userId와 동일")
    fun getUserId() {
        val token = jwtUtil.generateAccessToken(mockUser)

        assertThat(jwtUtil.getUserId(token)).isEqualTo(1L)
    }

    @Test
    @DisplayName("토큰에서 role을 추출하면 토큰 생성 시 넣은 role과 동일")
    fun getRole() {
        val token = jwtUtil.generateAccessToken(mockUser)

        assertThat(jwtUtil.getRole(token)).isEqualTo("USER")
    }

    @Test
    @DisplayName("refreshToken에서 userId 추출 시 accessToken과 동일")
    fun getUserIdFromRefreshToken() {
        val accessToken = jwtUtil.generateAccessToken(mockUser)
        val refreshToken = jwtUtil.generateRefreshToken(mockUser)

        assertThat(jwtUtil.getUserId(refreshToken)).isEqualTo(jwtUtil.getUserId(accessToken))
    }

    @Test
    @DisplayName("refreshToken 만료 시 ExpiredJwtException 발생")
    fun parseRefreshToken_expired() {
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", -1L)
        val expiredToken = jwtUtil.generateRefreshToken(mockUser)

        assertThatThrownBy { jwtUtil.parseToken(expiredToken) }
            .isInstanceOf(ExpiredJwtException::class.java)
    }

    companion object {
        private const val TEST_SECRET = "dGVzdFNlY3JldEtleUZvckp3dFRlc3RpbmdQdXJwb3Nl"
        private const val TEST_ACCESS_EXPIRATION = 900000L // 15분
        private const val TEST_REFRESH_EXPIRATION = 604800000L // 7일
    }
}