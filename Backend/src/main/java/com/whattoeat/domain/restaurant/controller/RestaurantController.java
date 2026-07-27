package com.whattoeat.domain.restaurant.controller;

import com.whattoeat.domain.restaurant.dto.RestaurantRequest;
import com.whattoeat.domain.restaurant.dto.RestaurantResponse;
import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.service.RestaurantService;
import com.whattoeat.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    //식당 추천조회
    @GetMapping("/recommend")
    public RsData<RestaurantResponse.Recommend> recommend(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String region1,
            @RequestParam(required = false) String region2,
            @RequestParam(required = false) String region3,
            @RequestParam(required = false) String region4
    ) {
        RestaurantResponse.Recommend restaurant = new RestaurantResponse.Recommend(
                restaurantService.recommend(category, region1, region2, region3, region4)
        );
        return RsData.success(restaurant, "식당 추천이 완료되었습니다.");
    }

    // 식당 조회 (kakaoPlaceId로 단건 조회 or 전체 목록)
    @GetMapping
    public RsData<?> getRestaurants(@RequestParam(required = false) String kakaoPlaceId) {
        if (kakaoPlaceId != null && !kakaoPlaceId.isBlank()) {
            Restaurant restaurant = restaurantService.findByKakaoPlaceId(kakaoPlaceId);
            return RsData.success(
                    new RestaurantResponse.Recommend(restaurant), "식당 조회가 완료되었습니다.");
        }

        List<RestaurantResponse.Recommend> result = restaurantService.findAll()
                .stream()
                .map(RestaurantResponse.Recommend::new)
                .toList();

        return RsData.success(result, "저장된 식당 목록입니다.");
    }

    // 프론트에서 선택한 식당만 저장
    @PostMapping
    public RsData<RestaurantResponse.Recommend> saveSelectedRestaurant(
            @Valid @RequestBody RestaurantRequest.FromKakao request
    ) {
        Restaurant restaurant = restaurantService.saveGetKakao(request);

        return RsData.success(
                new RestaurantResponse.Recommend(restaurant),
                "식당이 저장되었습니다."
        );
    }

    //식당 상세 조회
    @GetMapping("/{id}")
    public RsData<RestaurantResponse.Recommend> getRestaurantById(@PathVariable Long id) {
        Restaurant restaurant = restaurantService.findById(id);
        return RsData.success(new RestaurantResponse.Recommend(restaurant),
                "식당 조회가 완료 되었습니다.");
    }

}
