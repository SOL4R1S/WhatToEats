package com.whattoeat.domain.restaurantlist.repository;

import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface RestaurantListItemRepository extends JpaRepository<RestaurantListItem, Long> {
    @Query("""
        select item
          from RestaurantListItem item
         where item.id = :itemId
           and item.restaurantList.id = :listId
           and item.restaurantList.user.id = :userId
    """)
    Optional<RestaurantListItem> findListItem(Long itemId, Long listId, Long userId);

    boolean existsByRestaurantListIdAndRestaurantId(Long listId, Long restaurantListId);

    @Query("""
        select max(i.orderIndex)
          from RestaurantListItem i
         where i.restaurantList.id = :listId
    """)
    Integer findMaxOrderIndexByListId(Long listId);

    @Modifying(clearAutomatically = true)
    @Query("""
        update RestaurantListItem i
            set i.orderIndex = i.orderIndex + 1
          where i.restaurantList.id = :listId
            and i.orderIndex >= :orderIndex
    """)
    void incOrderIndex(Long listId, int orderIndex);

    @Query("""
        select item
          from RestaurantListItem item
         where item.restaurantList.id = :listId
         order by item.orderIndex asc
    """)
    List<RestaurantListItem> findItemsByListId(Long listId);
}
