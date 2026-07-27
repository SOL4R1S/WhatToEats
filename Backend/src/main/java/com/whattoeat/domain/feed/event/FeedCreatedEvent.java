package com.whattoeat.domain.feed.event;

public record FeedCreatedEvent(Long feedId, Long authorId) {
}
