package com.whattoeat.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.notification.entity.Notification;
import com.whattoeat.domain.notification.repository.NotificationRepository;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.NotificationNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User createUser(Long id, String nickname) {
        User user = User.builder().nickname(nickname).email(nickname + "@test.com").provider(Provider.LOCAL).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Feed createFeed(Long id, User author) {
        Feed feed = Feed.builder().user(author).content("맛집이네요").build();
        ReflectionTestUtils.setField(feed, "id", id);
        return feed;
    }

    @Test
    @DisplayName("팔로워 전원에게 새 글 알림이 생성된다")
    void createFeedNotifications_success() {
        User author = createUser(1L, "작성자");
        User follower1 = createUser(2L, "팔로워1");
        User follower2 = createUser(3L, "팔로워2");
        Feed feed = createFeed(10L, author);

        given(feedRepository.findById(10L)).willReturn(Optional.of(feed));
        given(followRepository.findByFollowing_Id(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of(Follow.of(follower1, author), Follow.of(follower2, author))));

        notificationService.createFeedNotifications(10L, 1L);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());

        List<Notification> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(n -> n.getReceiver().getId()).containsExactlyInAnyOrder(2L, 3L);
        assertThat(saved).allMatch(n -> n.getActor().getId().equals(1L));
        assertThat(saved).allMatch(n -> n.getFeed().getId().equals(10L));
        assertThat(saved).allMatch(n -> n.getMessage().contains("작성자"));
    }

    @Test
    @DisplayName("존재하지 않는 피드면 알림을 생성하지 않는다")
    void createFeedNotifications_feedNotFound() {
        given(feedRepository.findById(999L)).willReturn(Optional.empty());

        notificationService.createFeedNotifications(999L, 1L);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("팔로워가 없으면 알림을 생성하지 않는다")
    void createFeedNotifications_noFollowers() {
        User author = createUser(1L, "작성자");
        Feed feed = createFeed(10L, author);

        given(feedRepository.findById(10L)).willReturn(Optional.of(feed));
        given(followRepository.findByFollowing_Id(1L, Pageable.unpaged()))
                .willReturn(new PageImpl<>(List.of()));

        notificationService.createFeedNotifications(10L, 1L);

        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsRead_success() {
        User receiver = createUser(2L, "받는사람");
        User actor = createUser(1L, "작성자");
        Feed feed = createFeed(10L, actor);
        Notification notification = Notification.of(
                receiver, actor, feed, com.whattoeat.domain.notification.entity.NotificationType.NEW_FEED, "작성자님이 새 글을 작성했습니다."
        );

        given(notificationRepository.findByIdAndReceiverId(100L, 2L)).willReturn(Optional.of(notification));

        Notification result = notificationService.markAsRead(2L, 100L);

        assertThat(result.isRead()).isTrue();
    }

    @Test
    @DisplayName("본인 소유가 아니거나 존재하지 않는 알림이면 예외 발생")
    void markAsRead_notFound() {
        given(notificationRepository.findByIdAndReceiverId(100L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(2L, 100L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("알림 목록 조회")
    void getNotifications_success() {
        User receiver = createUser(2L, "받는사람");
        User actor = createUser(1L, "작성자");
        Feed feed = createFeed(10L, actor);
        Notification notification = Notification.of(
                receiver, actor, feed, com.whattoeat.domain.notification.entity.NotificationType.NEW_FEED, "작성자님이 새 글을 작성했습니다."
        );
        Pageable pageable = Pageable.ofSize(20);

        given(notificationRepository.findByReceiverIdOrderByIdDesc(2L, pageable))
                .willReturn(new PageImpl<>(List.of(notification), pageable, 1));

        Page<Notification> result = notificationService.getNotifications(2L, pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
