package com.whattoeat.domain.feedlike.repository;

import com.whattoeat.domain.feedlike.entity.FeedLike;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    boolean existsByFeed_IdAndUser_Id(Long feedId, Long userId);

    Optional<FeedLike> findByFeed_IdAndUser_Id(Long feedId, Long userId);

    void deleteByFeed_IdAndUser_Id(Long feedId, Long userId);

    @Query("""
    select fl.feed.id
    from FeedLike fl
    where fl.user.id = :userId
      and fl.feed.id in :feedIds
""")
    List<Long> findLikedFeedIdsByUserIdAndFeedIds(
            @Param("userId") Long userId,
            @Param("feedIds") List<Long> feedIds
    );
}
