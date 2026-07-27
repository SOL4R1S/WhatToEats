package com.whattoeat.domain.follow.repository;

import com.whattoeat.domain.follow.entity.Follow;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    @EntityGraph(attributePaths = "following")
    Page<Follow> findByFollower_Id(Long followerId, Pageable pageable);

    @EntityGraph(attributePaths = "follower")
    Page<Follow> findByFollowing_Id(Long followingId, Pageable pageable);

    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    long countByFollower_Id(Long followerId);

    long countByFollowing_Id(Long followingId);
}
