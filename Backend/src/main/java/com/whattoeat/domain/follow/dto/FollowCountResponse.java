package com.whattoeat.domain.follow.dto;

public record FollowCountResponse(
        Long userId,
        long followerCount,
        long followingCount
) {
}
