package com.whattoeat.domain.notification.entity;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_receiver_id", columnList = "receiver_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private NotificationType type;

    @Column(length = 500, nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    private Notification(User receiver, User actor, Feed feed, NotificationType type, String message) {
        this.receiver = receiver;
        this.actor = actor;
        this.feed = feed;
        this.type = type;
        this.message = message;
    }

    public static Notification of(User receiver, User actor, Feed feed, NotificationType type, String message) {
        return new Notification(receiver, actor, feed, type, message);
    }

    public void markAsRead() {
        this.read = true;
    }
}
