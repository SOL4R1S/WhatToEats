package com.whattoeat.domain.feed.dto.response;

import com.whattoeat.domain.feed.entity.Feed;

import java.time.LocalDateTime;

public record FeedDetailResponse(
        Long feedId,
        String content,
        String imageUrl,
        String nickname,
        String profileImage,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long restaurantId,
        String restaurantName
) {
    public static FeedDetailResponse from(Feed feed) {
        return new FeedDetailResponse(
                feed.getId(),
                feed.getContent(),
                feed.getImageUrl(),
                feed.getUser().getNickname(),
                feed.getUser().getProfileImage(),
                feed.getLikeCount(),
                feed.getCreatedAt(),
                feed.getUpdatedAt(),
                feed.getRestaurant() != null ? feed.getRestaurant().getId() : null,
                feed.getRestaurant() != null ? feed.getRestaurant().getName() : null
        );
    }
}
