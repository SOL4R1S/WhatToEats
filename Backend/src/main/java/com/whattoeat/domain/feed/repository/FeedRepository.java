package com.whattoeat.domain.feed.repository;

import com.whattoeat.domain.feed.entity.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;


public interface FeedRepository extends JpaRepository<Feed, Long> {

    @EntityGraph(attributePaths = {"user", "restaurant"})
    Page<Feed> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "restaurant"})
    Page<Feed> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "restaurant"})
    Page<Feed> findByRestaurantId(Long restaurantId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "restaurant"})
    Page<Feed> findByUser_IdNotInOrderByIdDesc(Collection<Long> userIds, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "restaurant"})
     Page<Feed> findByUser_IdInOrderByIdDesc(Collection<Long> userIds, Pageable pageable);
}
