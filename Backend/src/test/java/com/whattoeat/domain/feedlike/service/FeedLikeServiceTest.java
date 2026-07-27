package com.whattoeat.domain.feedlike.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.feedlike.dto.FeedLikeResponse;
import com.whattoeat.domain.feedlike.repository.FeedLikeRepository;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.AlreadyLikedFeedException;
import com.whattoeat.global.exception.FeedLikeNotFoundException;
import com.whattoeat.global.exception.FeedNotFoundException;
import com.whattoeat.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FeedLikeServiceTest {

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private FeedLikeService feedLikeService;

    @Autowired
    private FeedLikeRepository feedLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Test
    @DisplayName("like succeeds")
    void like() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");

        FeedLikeResponse response = feedLikeService.like(user.getId(), feed.getId());

        assertThat(response.feedId()).isEqualTo(feed.getId());
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.isLikedByMe()).isTrue();
        assertThat(feedLikeRepository.existsByFeed_IdAndUser_Id(feed.getId(), user.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("already liked feed fails")
    void likeAlreadyLikedFeed() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");
        feedLikeService.like(user.getId(), feed.getId());

        assertThatThrownBy(() -> feedLikeService.like(user.getId(), feed.getId()))
                .isInstanceOf(AlreadyLikedFeedException.class)
                .hasMessage("이미 좋아요한 피드입니다.");
    }

    @Test
    @DisplayName("unlike succeeds")
    void unlike() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");
        feedLikeService.like(user.getId(), feed.getId());

        FeedLikeResponse response = feedLikeService.unlike(user.getId(), feed.getId());

        assertThat(response.feedId()).isEqualTo(feed.getId());
        assertThat(response.likeCount()).isEqualTo(0);
        assertThat(response.isLikedByMe()).isFalse();
        assertThat(feedLikeRepository.existsByFeed_IdAndUser_Id(feed.getId(), user.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("unlike fails when relation does not exist")
    void unlikeNotFound() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");

        assertThatThrownBy(() -> feedLikeService.unlike(user.getId(), feed.getId()))
                .isInstanceOf(FeedLikeNotFoundException.class)
                .hasMessage("좋아요 관계가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("like status returns true")
    void getLikeStatusTrue() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");
        feedLikeService.like(user.getId(), feed.getId());

        FeedLikeResponse response = feedLikeService.getLikeStatus(user.getId(), feed.getId());

        assertThat(response.feedId()).isEqualTo(feed.getId());
        assertThat(response.likeCount()).isEqualTo(1);
        assertThat(response.isLikedByMe()).isTrue();
    }

    @Test
    @DisplayName("like status returns false")
    void getLikeStatusFalse() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");

        FeedLikeResponse response = feedLikeService.getLikeStatus(user.getId(), feed.getId());

        assertThat(response.feedId()).isEqualTo(feed.getId());
        assertThat(response.likeCount()).isEqualTo(0);
        assertThat(response.isLikedByMe()).isFalse();
    }

    @Test
    @DisplayName("like fails when user does not exist")
    void likeUserNotFound() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");

        assertThatThrownBy(() -> feedLikeService.like(999L, feed.getId()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    @DisplayName("like fails when feed does not exist")
    void likeFeedNotFound() {
        User user = saveUser("user");

        assertThatThrownBy(() -> feedLikeService.like(user.getId(), 999L))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessage("Feed not found: 999");
    }

    @Test
    @DisplayName("unlike fails when user does not exist")
    void unlikeUserNotFound() {
        User user = saveUser("user");
        Feed feed = saveFeed(user, "content");

        assertThatThrownBy(() -> feedLikeService.unlike(999L, feed.getId()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: 999");
    }

    @Test
    @DisplayName("unlike fails when feed does not exist")
    void unlikeFeedNotFound() {
        User user = saveUser("user");

        assertThatThrownBy(() -> feedLikeService.unlike(user.getId(), 999L))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessage("Feed not found: 999");
    }

    private User saveUser(String name) {
        return userRepository.save(User.builder()
                .loginId(name)
                .password("password")
                .kakaoId(name + "-kakao")
                .nickname(name)
                .email(name + "@test.com")
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build());
    }

    private Feed saveFeed(User user, String content) {
        Feed feed = Feed.builder()
                .user(user)
                .content(content)
                .build();
        ReflectionTestUtils.setField(feed, "updatedAt", LocalDateTime.now());
        return feedRepository.save(feed);
    }
}
