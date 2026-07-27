package com.whattoeat.domain.follow.entity;

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
        name = "follow",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follow_follower_following",
                        columnNames = {"follower_id", "following_id"}
                )
        },
        indexes = {
                @Index(name = "idx_follow_follower_id", columnList = "follower_id"),
                @Index(name = "idx_follow_following_id", columnList = "following_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    private Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    public static Follow of(User follower, User following) {
        return new Follow(follower, following);
    }
}
