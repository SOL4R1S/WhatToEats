package com.whattoeat.domain.feed.controller;

import com.whattoeat.domain.feed.dto.response.FeedListPageResponse;
import com.whattoeat.domain.feed.dto.request.FeedCreateRequest;
import com.whattoeat.domain.feed.dto.request.FeedUpdateRequest;
import com.whattoeat.domain.feed.dto.response.FeedDetailResponse;
import com.whattoeat.domain.feed.dto.response.FeedListResponse;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.whattoeat.domain.feed.service.FeedService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RsData<FeedDetailResponse> createFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("feed") @Valid FeedCreateRequest feedCreateRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        FeedDetailResponse response = feedService.createFeed(
                userDetails.getUser(), feedCreateRequest, image);
        return RsData.success(response, "피드가 생성되었습니다.");
    }

    @GetMapping
    public RsData<FeedListPageResponse> getFeeds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long restaurantId,
            Pageable pageable
    ) {
        Long currentUserId = userDetails != null ? userDetails.getUserId() : null;
        Page<FeedListResponse> page = feedService.getFeeds(currentUserId, userId, restaurantId, pageable);
        return RsData.success(FeedListPageResponse.from(page), "피드 목록 조회가 완료되었습니다.");
    }

    @GetMapping("/following")
    public RsData<FeedListPageResponse> getFollowingFeeds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        Page<FeedListResponse> page = feedService.getFollowingFeeds(userDetails.getUserId(), pageable);
        return RsData.success(FeedListPageResponse.from(page), "팔로잉 피드 조회가 완료되었습니다.");
    }

    @GetMapping("/recommend")
    public RsData<FeedListPageResponse> getRecommendedFeeds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        Page<FeedListResponse> page = feedService.getRandomRecommendedFeeds(userDetails.getUserId(), pageable);
        return RsData.success(FeedListPageResponse.from(page), "추천 피드 조회가 완료되었습니다.");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RsData<FeedDetailResponse> updateFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam("content") String content,
            @RequestParam(value = "restaurantId", required = false) Long restaurantId,
            @RequestParam(value = "deleteImage", required = false, defaultValue = "false")  Boolean deleteImage,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        FeedUpdateRequest feedUpdateRequest = new FeedUpdateRequest(content, restaurantId,
                deleteImage);
        FeedDetailResponse response = feedService.updateFeed(
                id, userDetails.getUserId(), feedUpdateRequest, image);
        return RsData.success(response, "피드가 수정되었습니다.");
    }

    @DeleteMapping("/{id}")
    public RsData<Void> deleteFeed(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        feedService.deleteFeed(id, userDetails.getUserId());
        return RsData.success(null, "피드가 삭제되었습니다.");
    }

}
