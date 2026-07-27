package com.whattoeat.domain.feed.dto.request;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.user.entity.User;
import jakarta.validation.constraints.*;

public record FeedCreateRequest(
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max=1000, message = "내용은 1000자를 넘을 수 없습니다.")
        String content,

        @Positive(message = "음수 Id는 올 수 없습니다.")
        Long restaurantId
) {
        public Feed toEntity(User user, Restaurant restaurant, String imageUrl) {
                return Feed.builder()
                        .user(user)
                        .restaurant(restaurant)
                        .content(content)
                        .imageUrl(imageUrl)
                        .build();
        }
}
