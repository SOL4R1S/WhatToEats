package com.whattoeat.domain.restaurant.dto;

import com.whattoeat.domain.restaurant.entity.Restaurant;

import java.time.LocalDateTime;

public class RestaurantResponse {

    public record KakaoRestaurant(
            Long id,
            String kakaoPlaceId,
            String name,
            String category,
            String address,
            String roadAddress,
            String region1,
            String region2,
            String region3,
            String region4,
            String phone,
            Double lat,
            Double lng
    ) {
    }

    public record Recommend(
            Long id,
            String kakaoPlaceId,
            String name,
            String category,
            String address,
            String roadAddress,
            String region1,
            String region2,
            String region3,
            String region4,
            String phone,
            Double lat,
            Double lng,
            LocalDateTime createdAt
    ) {
        public Recommend(Restaurant restaurant) {
            this(
                    restaurant.getId(),
                    restaurant.getKakaoPlaceId(),
                    restaurant.getName(),
                    restaurant.getCategory().name(),
                    restaurant.getAddress(),
                    restaurant.getRoadAddress(),
                    restaurant.getRegion1(),
                    restaurant.getRegion2(),
                    restaurant.getRegion3(),
                    restaurant.getRegion4(),
                    restaurant.getPhone(),
                    restaurant.getLat(),
                    restaurant.getLng(),
                    restaurant.getCreatedAt()
            );
        }
    }
}
