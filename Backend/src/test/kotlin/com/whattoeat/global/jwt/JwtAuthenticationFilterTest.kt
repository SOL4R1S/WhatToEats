package com.whattoeat.global.jwt

import com.whattoeat.domain.user.entity.Role
import com.whattoeat.domain.user.entity.User
import com.whattoeat.global.security.CustomUserDetails
import com.whattoeat.global.security.CustomUserDetailsService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.InjectMocks
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.assertj.core.api.Assertions.assertThat

@ExtendWith(MockitoExtension::class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    lateinit var jwtUtil: JwtUtil

    @Mock
    lateinit var customUserDetailsService: CustomUserDetailsService

    @Mock
    lateinit var redisTemplate: RedisTemplate<String, String>

    @InjectMocks
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보 저장")
    fun validToken_authenticates() {
        val mockUser = mock(User::class.java)
        `when`(mockUser.role).thenReturn(Role.USER)
        val customUserDetails = CustomUserDetails(mockUser)

        val claims = mock(Claims::class.java)
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims)
        given(claims.get("tokenType", String::class.java)).willReturn("access")
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L)
        given(customUserDetailsService.loadUserByUsername("1")).willReturn(customUserDetails)

        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $VALID_TOKEN")

        jwtAuthenticationFilter.doFilter(request, MockHttpServletResponse(), mock(FilterChain::class.java))

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNotNull()
        assertThat(authentication!!.isAuthenticated).isTrue()
    }

    @Test
    @DisplayName("위변조된 토큰이면 인증 정보 없이 다음 필터로 진행 (permitAll 엔드포인트를 막지 않기 위함)")
    fun invalidToken_continuesChain() {
        given(jwtUtil.parseToken(INVALID_TOKEN)).willThrow(JwtException("invalid token"))

        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $INVALID_TOKEN")
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @DisplayName("토큰이 없으면 SecurityContext에 인증 정보 없음")
    fun noToken_noAuth() {
        jwtAuthenticationFilter.doFilter(
            MockHttpServletRequest(), MockHttpServletResponse(), mock(FilterChain::class.java)
        )

        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @DisplayName("블랙리스트에 있는 토큰으로 요청 시 인증 정보 없이 다음 필터로 진행")
    fun blacklistedToken_skipsAuthentication() {
        val claims = mock(Claims::class.java)
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims)
        given(claims.get("tokenType", String::class.java)).willReturn("access")
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L)
        given(redisTemplate.hasKey("blacklist:$VALID_TOKEN")).willReturn(true)

        val request = MockHttpServletRequest()
        request.addHeader("Authorization", "Bearer $VALID_TOKEN")
        val response = MockHttpServletResponse()
        val filterChain = mock(FilterChain::class.java)

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertThat(SecurityContextHolder.getContext().authentication).isNull()
    }

    @Test
    @DisplayName("accessToken 쿠키로 인증")
    fun accessTokenCookie_authenticates() {
        val mockUser = mock(User::class.java)
        `when`(mockUser.role).thenReturn(Role.USER)
        val customUserDetails = CustomUserDetails(mockUser)

        val claims = mock(Claims::class.java)
        given(jwtUtil.parseToken(VALID_TOKEN)).willReturn(claims)
        given(claims.get("tokenType", String::class.java)).willReturn("access")
        given(jwtUtil.getUserId(VALID_TOKEN)).willReturn(1L)
        given(customUserDetailsService.loadUserByUsername("1")).willReturn(customUserDetails)

        val req = MockHttpServletRequest()
        req.setCookies(Cookie("accessToken", VALID_TOKEN))

        jwtAuthenticationFilter.doFilter(req, MockHttpServletResponse(), mock(FilterChain::class.java))

        val authentication = SecurityContextHolder.getContext().authentication
        assertThat(authentication).isNotNull()
        assertThat(authentication!!.isAuthenticated).isTrue()
    }

    companion object {
        private const val VALID_TOKEN = "valid.jwt.token"
        private const val INVALID_TOKEN = "invalid.token"
    }
}