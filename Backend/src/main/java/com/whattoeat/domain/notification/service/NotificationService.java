package com.whattoeat.domain.notification.service;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.notification.entity.Notification;
import com.whattoeat.domain.notification.entity.NotificationType;
import com.whattoeat.domain.notification.repository.NotificationRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.NotificationNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;

    // REQUIRES_NEW: AFTER_COMMIT 콜백에서 호출되면 스레드에 남아있는 종료된 트랜잭션에
    // 편승해 flush가 조용히 사라지므로, 항상 새 트랜잭션을 시작해야 한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFeedNotifications(Long feedId, Long authorId) {
        Feed feed = feedRepository.findById(feedId).orElse(null);
        if (feed == null) {
            return;
        }

        List<Follow> follows = followRepository.findByFollowing_Id(authorId, Pageable.unpaged()).getContent();
        if (follows.isEmpty()) {
            return;
        }

        User author = follows.get(0).getFollowing();
        String message = author.getNickname() + "님이 새 글을 작성했습니다.";

        List<Notification> notifications = follows.stream()
                .map(follow -> Notification.of(follow.getFollower(), author, feed, NotificationType.NEW_FEED, message))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByReceiverIdOrderByIdDesc(userId, pageable);
    }

    @Transactional
    public Notification markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.markAsRead();

        return notification;
    }
}
