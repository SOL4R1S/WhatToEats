package com.whattoeat.domain.follow.controller;

import com.whattoeat.domain.follow.dto.FollowCountResponse;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.domain.follow.dto.FollowResponse;
import com.whattoeat.domain.follow.dto.FollowUserResponse;
import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.service.FollowService;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followingId}")
    public ResponseEntity<RsData<FollowResponse>>follow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long followingId) {
        Follow follow = followService.follow(userDetails.getUserId(), followingId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(FollowResponse.from(follow), "팔로우했습니다."));
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<RsData<Void>>  unfollow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long followingId) {
        followService.unfollow(userDetails.getUserId(), followingId);
        return ResponseEntity.ok(
                RsData.success(null, "언팔로우했습니다."));
    }

    @GetMapping("/followings")
    public ResponseEntity<RsData<Page<FollowUserResponse>>> getFollowings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Long currentUserId = userDetails.getUserId();
        Page<FollowUserResponse> response = followService.getFollowings(currentUserId, pageable)
                .map(follow -> toUserResponse(follow.getFollowing(), currentUserId));
        return ResponseEntity.ok(RsData.success(response));
    }

    @GetMapping("/followers")
    public ResponseEntity<RsData<Page<FollowUserResponse>>> getFollowers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Long currentUserId = userDetails.getUserId();
        Page<FollowUserResponse> response = followService.getFollowers(currentUserId, pageable)
                .map(follow -> toUserResponse(follow.getFollower(), currentUserId));
        return ResponseEntity.ok(RsData.success(response));
    }

    @GetMapping("/users/{userId}/count")
    public ResponseEntity<RsData<FollowCountResponse>> getFollowerCounts(@PathVariable Long userId) {
        long followerCount = followService.getFollowerCount(userId);
        long followingCount = followService.getFollowingCount(userId);
        return ResponseEntity.ok(RsData.success(
                new FollowCountResponse(userId, followerCount, followingCount)));
    }

    @GetMapping("/users/{userId}/followings")
    public ResponseEntity<RsData<Page<FollowUserResponse>>> getUserFollowings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            Pageable pageable
    ){
        Long currentUserId = userDetails.getUserId();
        Page<FollowUserResponse> response = followService.getFollowings(userId,pageable)
                .map(follow -> toUserResponse(follow.getFollowing(), currentUserId));
        return ResponseEntity.ok(RsData.success(response));
    }

    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<RsData<Page<FollowUserResponse>>> getUserFollowers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            Pageable pageable
    ){
        Long currentUserId = userDetails.getUserId();
        Page<FollowUserResponse> response = followService.getFollowers(userId,pageable)
                .map(follow -> toUserResponse(follow.getFollower(), currentUserId));
        return ResponseEntity.ok(RsData.success(response));
    }


    private FollowUserResponse toUserResponse(User user, Long currentUserId) {
        return FollowUserResponse.from(
                user,
                followService.isFollowing(currentUserId, user.getId()));
    }

}
