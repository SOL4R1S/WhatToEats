package com.whattoeat.domain.feed.service;

import com.whattoeat.domain.comment.repository.CommentRepository;
import com.whattoeat.domain.feed.dto.request.FeedCreateRequest;
import com.whattoeat.domain.feed.dto.request.FeedUpdateRequest;
import com.whattoeat.domain.feed.dto.response.FeedDetailResponse;
import com.whattoeat.domain.feed.dto.response.FeedListResponse;
import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.event.FeedCreatedEvent;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.feedlike.repository.FeedLikeRepository;
import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.repository.RestaurantRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.FeedNotFoundException;
import com.whattoeat.global.upload.ImageUploadService;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final RestaurantRepository restaurantRepository;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentRepository commentRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final ImageUploadService imageUploadService;


    @Transactional
    public FeedDetailResponse createFeed(
            User user, FeedCreateRequest feedCreateRequest, MultipartFile image) throws IOException {
        Restaurant restaurant = feedCreateRequest.restaurantId() != null
                ? restaurantRepository.findById(feedCreateRequest.restaurantId()).orElse(null)
                : null;

        String imageUrl = (image != null && !image.isEmpty())
                ? imageUploadService.upload(image)
                : null;


        Feed feed = feedRepository.save(feedCreateRequest.toEntity(user, restaurant, imageUrl));
        eventPublisher.publishEvent(new FeedCreatedEvent(feed.getId(), user.getId()));
        return FeedDetailResponse.from(feed);
    }

    @Transactional(readOnly = true)
    public Page<FeedListResponse> getFeeds(Long currentUserId, Long userId, Long restaurantId, Pageable pageable) {
        Page<Feed> feeds;
        if (userId != null) {
            feeds = feedRepository.findByUserId(userId, pageable);
        } else if (restaurantId != null) {
            feeds = feedRepository.findByRestaurantId(restaurantId, pageable);
        } else {
            feeds = feedRepository.findAllByOrderByIdDesc(pageable);
        }

        List<Feed> feedContents = feeds.getContent();

        Map<Long, Long> commentCounts = countCommentByFeedIds(feedContents);
        Set<Long> likedFeedIds = findLikedFeedIds(currentUserId, feedContents);
        return feeds.map(feed -> FeedListResponse
                .from(feed, commentCounts.getOrDefault(feed.getId(), 0L),
                        likedFeedIds.contains(feed.getId())
                ));
    }


    private Map<Long, Long> countCommentByFeedIds(List<Feed> feeds) {
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        if (feedIds.isEmpty()) return Map.of();
        return commentRepository.countByFeedIds(feedIds)
                .stream().collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> row[1] == null ? 0L : ((Number) row[1]).longValue()
                        )
                );
    }

    private Set<Long> findLikedFeedIds(Long currentUserId, List<Feed> feeds) {
        if (currentUserId == null || feeds.isEmpty()) {
            return Set.of();
        }

        List<Long> feedIds = feeds.stream()
                .map(Feed::getId)
                .toList();

        return new HashSet<>(
                feedLikeRepository.findLikedFeedIdsByUserIdAndFeedIds(currentUserId, feedIds)
        );
    }

    @Transactional(readOnly = true)
    public Page<FeedListResponse> getFollowingFeeds(Long userId, Pageable pageable) {
        List<Long> followingUserIds = followRepository.findByFollower_Id(userId, Pageable.unpaged())
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .toList();

        if (followingUserIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Feed> feeds = feedRepository.findByUser_IdInOrderByIdDesc(followingUserIds, pageable);
        List<Feed> feedContents = feeds.getContent();

        Map<Long, Long> commentCounts = countCommentByFeedIds(feedContents);
        Set<Long> likedFeedIds = findLikedFeedIds(userId, feedContents);

        return feeds.map(feed -> FeedListResponse
                .from(feed, commentCounts.getOrDefault(feed.getId(), 0L),
                        likedFeedIds.contains(feed.getId())
                ));
    }

    @Transactional(readOnly = true)
    public Page<FeedListResponse> getRandomRecommendedFeeds(Long userId, Pageable pageable) {
        if (userId == null) return Page.empty(pageable);

        List<Long> followingUserIds = followRepository.findByFollower_Id(userId,
                        Pageable.unpaged())
                .stream()
                .map(follow -> follow.getFollowing().getId())
                .toList();

        Page<Feed> feeds;
        if (followingUserIds.isEmpty()) {
            feeds = feedRepository.findAllByOrderByIdDesc(pageable);
        } else {
            feeds = feedRepository.findByUser_IdNotInOrderByIdDesc(followingUserIds, pageable);
        }

        List<Feed> feedContents = feeds.getContent();
        Map<Long, Long> commentCounts = countCommentByFeedIds(feedContents);
        Set<Long> likedFeedIds = findLikedFeedIds(userId, feedContents);

        return feeds.map(feed -> FeedListResponse
                .from(feed, commentCounts.getOrDefault(feed.getId(), 0L),
                        likedFeedIds.contains(feed.getId())));
    }


    @Transactional
    public FeedDetailResponse updateFeed(
            Long feedId, Long currentUserId, FeedUpdateRequest request, MultipartFile image) throws IOException {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (!feed.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("본인 피드만 수정할 수 있습니다.");
        }

        Restaurant restaurant = request.restaurantId() != null
                ? restaurantRepository.findById(request.restaurantId()).orElse(null)
                : null;

        String imageUrl = feed.getImageUrl();
        if (request.deleteImage()) {
            imageUrl = null;
        }
        if (image != null && !image.isEmpty()) imageUrl = imageUploadService.upload(image);

        feed.update(request.content(), restaurant, imageUrl);

        return FeedDetailResponse.from(feedRepository.save(feed));
    }

    @Transactional
    public void deleteFeed(Long feedId, Long currentUserId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (!feed.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("본인 피드만 삭제할 수 있습니다.");
        }

        feedRepository.delete(feed);
    }


}
