package com.whattoeat.domain.auth.service;

import com.whattoeat.domain.auth.dto.*;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.*;
import com.whattoeat.global.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public User signup(SignUpRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateLoginIdException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }
        User user = User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .email(request.loginId())
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public AuthResult signupAndLogin(SignUpRequest request) {
        User user = signup(request);
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user.getId(), refreshToken);
        return new AuthResult(accessToken, refreshToken, AuthUserResponse.from(user));
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                refreshToken,
                Duration.ofDays(7)
        );
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        Long userId = jwtUtil.getUserId(refreshToken);
        String savedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new InvalidCredentialsException("유효하지 않은 refreshToken입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()->new InvalidCredentialsException("유효하지 않은 refreshToken입니다."));

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        saveRefreshToken(userId, newRefreshToken);
        return  new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new InvalidCredentialsException("아이디/비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("아이디/비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user.getId(), refreshToken);
        return new AuthResult(accessToken, refreshToken, AuthUserResponse.from(user));
    }
    // OAuth2 로그인은 백엔드 도메인으로 직접 리다이렉트되므로, 그 응답에서 쿠키를 바로 굽지 않고
    // 1회용 코드만 프론트로 넘긴 뒤 프론트가 /api 프록시로 교환하게 해서 쿠키가 프론트 도메인에 스코프되도록 한다.
    public String createOAuthCode(Long userId) {
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "oauth-code:" + code,
                String.valueOf(userId),
                Duration.ofSeconds(60)
        );
        return code;
    }

    @Transactional
    public AuthResult exchangeOAuthCode(String code) {
        String key = "oauth-code:" + code;
        String userIdValue = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);

        if (userIdValue == null) {
            throw new InvalidCredentialsException("유효하지 않거나 만료된 코드입니다.");
        }

        User user = userRepository.findById(Long.parseLong(userIdValue))
                .orElseThrow(() -> new InvalidCredentialsException("유효하지 않은 코드입니다."));

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        saveRefreshToken(user.getId(), refreshToken);
        return new AuthResult(accessToken, refreshToken, AuthUserResponse.from(user));
    }

    public void logout(String token){
        long remaining = jwtUtil.getRemainingExpiration(token);
        if (remaining > 0) {
            redisTemplate.opsForValue().set("blacklist:" + token, "logout", remaining, TimeUnit.MILLISECONDS);
        }
    }
}
