package com.whattoeat.domain.feedlike.entity;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "feed_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_like_feed_user",
                        columnNames = {"feed_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_feed_like_feed_id", columnList = "feed_id"),
                @Index(name = "idx_feed_like_user_id", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private FeedLike(Feed feed, User user) {
        this.feed = feed;
        this.user = user;
    }

    public static FeedLike of(Feed feed, User user) {
        return new FeedLike(feed, user);
    }
}
