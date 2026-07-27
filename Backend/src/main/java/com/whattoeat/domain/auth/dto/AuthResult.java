package com.whattoeat.domain.auth.dto;


public record AuthResult(
        String accessToken,
        String refreshToken,
        AuthUserResponse userProfile
) {
}
