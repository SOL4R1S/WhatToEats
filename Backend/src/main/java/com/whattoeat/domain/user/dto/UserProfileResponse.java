package com.whattoeat.domain.user.dto;

import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String nickname,
        String profileImage,
        String email,
        Provider provider,
        LocalDateTime createdAt,
        boolean isOwnProfile,
        boolean isFollowing) {

    public static UserProfileResponse from(User user, Long currentUserId, boolean isFollowing) {
        boolean isOwn = user.getId().equals(currentUserId);
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getEmail(),
                user.getProvider(),
                user.getCreatedAt(),
                isOwn,
                isOwn ? false : isFollowing);

    }
}
