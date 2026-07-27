package com.whattoeat.domain.restaurantlist.dto;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.SavedRestaurantList;

import java.time.LocalDateTime;
import java.util.List;

public record SavedRestaurantListResponse(
        Long listId,
        Long userId,
        String nickname,
        String title,
        String description,
        MoodTag moodTag,
        List<RestaurantListResponse.RestaurantListItemDetail> items,
        LocalDateTime savedAt
) {

    public static SavedRestaurantListResponse from(
            SavedRestaurantList savedRestaurantList
    ) {
        RestaurantList restaurantList =
                savedRestaurantList.getRestaurantList();

        return new SavedRestaurantListResponse(
                restaurantList.getId(),
                restaurantList.getUser().getId(),
                restaurantList.getUser().getNickname(),
                restaurantList.getTitle(),
                restaurantList.getDescription(),
                restaurantList.getMoodTag(),
                restaurantList.getItems().stream()
                        .map(RestaurantListResponse.RestaurantListItemDetail::new)
                        .toList(),
                savedRestaurantList.getCreatedAt()
        );
    }
}
