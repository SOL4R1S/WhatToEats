package com.whattoeat.domain.follow.dto;

import com.whattoeat.domain.follow.entity.Follow;
import java.time.LocalDateTime;

public record FollowResponse(
        Long followId,
        Long followerId,
        Long followingId,
        LocalDateTime createdAt) {

    public static FollowResponse from(Follow follow) {
        return new FollowResponse(
                follow.getId(),
                follow.getFollower().getId(),
                follow.getFollowing().getId(),
                follow.getCreatedAt());
    }
}
