package com.whattoeat.domain.restaurantlist.dto;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RestaurantListRequest {
    public record RestaurantList(
            @NotBlank(message = "제목은 필수입니다.")
            String title,
            String description,
            MoodTag moodTag
    ) {}

    public record RestaurantListItem(
        @NotNull
        Long restaurantId,

        String memo,

        @Min(value = 1, message = "순서는 1 이상이어야 합니다.")
        Integer orderIndex
    ) {}
}
