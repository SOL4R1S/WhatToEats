package com.whattoeat.domain.comment.entity

import com.whattoeat.domain.feed.entity.Feed
import com.whattoeat.domain.user.entity.User
import com.whattoeat.global.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "feed_comment",
    indexes = [
        Index(name = "idx_feed_comment_feed_id", columnList = "feed_id"),
        Index(name = "idx_feed_comment_user_id", columnList = "user_id"),
    ],
)
class Comment(
    feed: Feed,
    user: User,
    content: String,
) : BaseEntity() {

    /** 소프트삭제 시각. NULL=정상, NOT NULL=숨김(롤백 가능). */
    @Column(name = "deleted_at")
    var deletedAt: java.time.LocalDateTime? = null
        protected set

    fun softDelete() {
        if (this.deletedAt == null) this.deletedAt = java.time.LocalDateTime.now()
    }

    fun restore() {
        this.deletedAt = null
    }

    val isDeleted: Boolean
        get() = deletedAt != null
    @field:JoinColumn(name = "feed_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var feed: Feed = feed
        protected set

    @field:JoinColumn(name = "user_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    var user: User = user
        protected set

    @field:Column(nullable = false, length = 500)
    var content: String = content
        protected set
}
