package com.whattoeat.domain.feedlike.dto;


public record FeedLikeResponse(
        Long feedId,
        Integer likeCount,
        boolean isLikedByMe) {

    public static FeedLikeResponse of(Long feedId, Integer likeCount, boolean isLikedByMe) {
        return new FeedLikeResponse(feedId, likeCount, isLikedByMe);
    }
}
