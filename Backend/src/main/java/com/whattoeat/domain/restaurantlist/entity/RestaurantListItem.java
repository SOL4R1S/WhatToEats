package com.whattoeat.domain.restaurantlist.entity;

import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant_list_item",
    uniqueConstraints = {
      @UniqueConstraint(
              name = "uk_restaurant_list_item_list_restaurant",
              columnNames = {"list_id", "restaurant_id"}
      )
    },
    indexes = {
        @Index(name = "idx_restaurant_list_item_list_id", columnList = "list_id"),
        @Index(name = "idx_restaurant_list_item_restaurant_id", columnList = "restaurant_id")
    }
)
@Getter
@NoArgsConstructor
public class RestaurantListItem extends BaseEntity {
    // 레스토랑 리스트 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private RestaurantList restaurantList;

    // 식당 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 순서
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    // 한줄평
    @Column(name = "memo", length = 200)
    private String memo;

    public RestaurantListItem(RestaurantList restaurantList, Restaurant restaurant, String memo, Integer orderIndex) {
        this.restaurantList = restaurantList;
        this.restaurant = restaurant;
        this.memo = memo;
        this.orderIndex = orderIndex;
    }

    public void updateListItem(Integer orderIndex, String memo) {
        this.orderIndex = orderIndex;
        this.memo = memo;
    }
}
