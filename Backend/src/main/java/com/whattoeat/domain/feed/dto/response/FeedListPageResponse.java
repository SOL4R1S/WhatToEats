package com.whattoeat.domain.feed.dto.response;


import org.springframework.data.domain.Page;

import java.util.List;

public record FeedListPageResponse(
        List<FeedListResponse> feeds,
        int totalPages,
        long totalElements,
        int page,
        int size
) {
    public static FeedListPageResponse from(Page<FeedListResponse> page) {
        return new FeedListPageResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }
}
