package com.whattoeat.domain.comment.repository;

import com.whattoeat.domain.comment.entity.Comment;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByFeedId(Long feedId);

    // FeedListResponse에 댓글 수 넣기 위한 카운트 쿼리 추가
    long countByFeedId(Long feedId);

    @Query("SELECT c.feed.id, COUNT(c) FROM Comment c WHERE c.feed.id IN :feedIds GROUP BY c.feed.id")
    List<Object[]> countByFeedIds(@Param("feedIds") List<Long> feedIds);
}
