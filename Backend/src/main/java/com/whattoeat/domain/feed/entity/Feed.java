package com.whattoeat.domain.feed.entity;

import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "feeds",
        indexes = {
                @Index(name = "idx_feed_user_id", columnList = "user_id"),
                @Index(name = "idx_feed_restaurant_id", columnList = "restaurant_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter(AccessLevel.PUBLIC)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Setter(AccessLevel.PUBLIC)
    @Column(length = 1000, nullable = false)
    private String content;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Setter(AccessLevel.PUBLIC)
    private String imageUrl;

    @Builder
    public Feed(User user, Restaurant restaurant, String content, String imageUrl, Integer likeCount) {
        this.user = user;
        this.restaurant = restaurant;
        this.content = content;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount!=null?likeCount:0;
    }

    public void update(String content, Restaurant restaurant, String imageUrl) {
        this.content = content;
        this.restaurant = restaurant;
        this.imageUrl = imageUrl;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
