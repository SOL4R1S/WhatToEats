package com.whattoeat.domain.restaurantlist.dto;

public record SavedRestaurantListStatusResponse(
        Long restaurantListId,
        boolean saved
) {
    public static SavedRestaurantListStatusResponse of(Long restaurantListId, boolean saved) {
        return new SavedRestaurantListStatusResponse(restaurantListId, saved);
    }
}
