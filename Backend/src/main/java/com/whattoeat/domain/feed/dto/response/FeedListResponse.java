package com.whattoeat.domain.feed.dto.response;

import com.whattoeat.domain.feed.entity.Feed;

import java.time.LocalDateTime;

public record FeedListResponse(
        Long feedId,
        String content,
        String imageUrl,
        // 프론트에서 피드 카드 클릭 시 /profile/{userId} 링크 만들어 본인이 쓴 글 여부 판별 가능
        Long userId,
        String nickname,
        String profileImage,
        int likeCount,
        boolean isLikedByMe,
        long commentCount,
        // id 만 있으면 추가 api 호출하여 피드 목록 로딩이 느려지고, 반대로 name만 있으면 링크 불가
        Long restaurantId,
        String restaurantName,
        LocalDateTime createdAt
) {
    public static FeedListResponse from(Feed feed, long commentCount, boolean isLikedByMe) {
        return new FeedListResponse(
                feed.getId(),
                feed.getContent(),
                feed.getImageUrl(),
                feed.getUser().getId(),
                feed.getUser().getNickname(),
                feed.getUser().getProfileImage(),
                feed.getLikeCount(),
                isLikedByMe,
                commentCount,
                feed.getRestaurant() != null ? feed.getRestaurant().getId() : null,
                feed.getRestaurant() != null ? feed.getRestaurant().getName() : null,
                feed.getCreatedAt()
        );
    }
}
