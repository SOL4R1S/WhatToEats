package com.whattoeat.global.jwt;

import com.whattoeat.global.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            String tokenType = claims.get("tokenType", String.class);

            if ("access".equals(tokenType)) {
                Long userId = jwtUtil.getUserId(token);
                if (!redisTemplate.hasKey("blacklist:" + token)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (JwtException e) {
            // 토큰이 만료/위변조/유효하지 않아도 여기서 바로 401을 응답하지 않는다.
            // 이 필터는 permitAll 여부와 상관없이 모든 요청에서 실행되므로, 여기서 응답을
            // 끊어버리면 공개 엔드포인트까지 막힌다. 인증 정보만 설정하지 않고 체인을 계속 진행해서,
            // 실제로 인증이 필요한 요청만 Spring Security 인가 단계에서 401 처리되도록 한다.
        }
        filterChain.doFilter(request, response);
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/api/v1/auth/reissue".equals(path) || "/api/v1/auth/logout".equals(path);
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
