package com.whattoeat.domain.auth.dto;

import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;

import java.time.LocalDateTime;

public record AuthUserResponse(
        Long userId,
        String nickname,
        String profileImage,
        String email,
        Provider provider,
        Role role,
        LocalDateTime createAt
) {
    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getEmail(),
                user.getProvider(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
