package com.whattoeat.domain.comment.entity;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feed_comment")
@Getter
@NoArgsConstructor
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Builder
    public Comment(Feed feed, User user, String content) {
        this.feed = feed;
        this.user = user;
        this.content = content;
    }
}
