package com.whattoeat.domain.restaurantlist.repository;

import com.whattoeat.domain.restaurantlist.entity.SavedRestaurantList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedRestaurantListRepository extends JpaRepository<SavedRestaurantList, Long> {
    boolean existsByUserIdAndRestaurantListId(Long userId, Long restaurantListId);

    Optional<SavedRestaurantList> findByUserIdAndRestaurantListId(Long userId, Long id);

    @EntityGraph(attributePaths = {
            "restaurantList",
            "restaurantList.user"
    })
    Page<SavedRestaurantList> findByUserId(Long userId, Pageable pageable);
}
