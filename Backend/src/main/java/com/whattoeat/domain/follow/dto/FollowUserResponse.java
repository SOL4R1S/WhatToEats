package com.whattoeat.domain.follow.dto;

import com.whattoeat.domain.user.entity.User;
import java.time.LocalDateTime;

public record FollowUserResponse(
        Long userId,
        String nickname,
        String profileImage,
        boolean isFollowedByMe,
        LocalDateTime createdAt) {

    public static FollowUserResponse from(User user, boolean isFollowedByMe) {
        return new FollowUserResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                isFollowedByMe,
                user.getCreatedAt());
    }
}
