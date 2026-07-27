package com.whattoeat.domain.feedlike.controller;

import com.whattoeat.domain.feedlike.dto.FeedLikeResponse;
import com.whattoeat.domain.feedlike.service.FeedLikeService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feeds/{feedId}/like")
@RequiredArgsConstructor
public class FeedLikeController {

    private final FeedLikeService feedLikeService;

    @PostMapping
    public ResponseEntity<RsData<FeedLikeResponse>> like(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId) {
        FeedLikeResponse response = feedLikeService.like(userDetails.getUserId(), feedId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(response, "좋아요를 눌렀습니다."));
    }

    @DeleteMapping
    public ResponseEntity<RsData<FeedLikeResponse>> unlike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId) {
        FeedLikeResponse response = feedLikeService.unlike(userDetails.getUserId(), feedId);
        return ResponseEntity.ok(RsData.success(response, "좋아요를 취소했습니다."));
    }

    @GetMapping
    public ResponseEntity<RsData<FeedLikeResponse>> isLiked(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId) {
        FeedLikeResponse response = feedLikeService.getLikeStatus(userDetails.getUserId(), feedId);

        return ResponseEntity.ok(RsData.success(response));
    }
}
