package com.whattoeat.domain.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RestaurantRequest {

    // 프론트에서 카카오 검색할 때 보내는 요청
    public record KakaoSearch(
            @NotBlank(message = "검색어는 필수입니다.")
            String keyword,

            Double lng,
            Double lat,
            Integer radius,
            Integer page
    ) {
    }

    // 카카오 응답을 Restaurant 저장용을 변환
    public record FromKakao(
            @NotBlank(message = "카카오 장소 ID는 필수입니다.")
            String kakaoPlaceId,
            @NotBlank(message = "식당명은 필수입니다.")
            String name,
            String categoryName,
            @NotBlank(message = "주소는 필수입니다.")
            String address,
            String roadAddress,
            String region1,
            String region2,
            String region3,
            String region4,
            String phone,
            @NotNull(message = "위도는 필수입니다.")
            Double lat,
            @NotNull(message = "경도는 필수입니다.")
            Double lng
    ) {
    }
}
