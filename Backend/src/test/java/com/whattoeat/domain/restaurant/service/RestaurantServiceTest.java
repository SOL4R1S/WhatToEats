package com.whattoeat.domain.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.repository.RestaurantRepository;
import com.whattoeat.global.exception.RestaurantNotFoundException;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant createRestaurant(String kakaoId, String name, Category category, String region1) {
        return new Restaurant(kakaoId, name, category, "주소", "도로명주소",
                region1, "강남구", "역삼동", null, "02-0000-0000", 37.5, 127.0);
    }

    @Test
    void recommend_필터없이_전체에서_랜덤_추천() {
        Restaurant r1 = createRestaurant("kakao-1", "식당A", Category.KOREAN, "서울");
        Restaurant r2 = createRestaurant("kakao-2", "식당B", Category.WESTERN, "서울");
        given(restaurantRepository
                .findRecommended(null, null, null, null, null))
                .willReturn(List.of(r1, r2));

        Restaurant result = restaurantService
                .recommend(null, null, null, null, null);

        assertThat(result).isIn(r1, r2);
        then(restaurantRepository).should()
                .findRecommended(null, null, null, null, null);
    }

    @Test
    void recommend_카테고리_필터로_추천() {
        Restaurant r1 = createRestaurant("kakao-1", "한식당A", Category.KOREAN, "서울");
        Restaurant r2 = createRestaurant("kakao-2", "한식당B", Category.KOREAN, "부산");
        given(restaurantRepository
                .findRecommended(Category.KOREAN, null, null, null, null))
                .willReturn(List.of(r1, r2));

        Restaurant result = restaurantService
                .recommend(Category.KOREAN, null, null, null, null);

        assertThat(result).isIn(r1, r2);
        assertThat(result.getCategory()).isEqualTo(Category.KOREAN);
    }

    @Test
    void recommend_결과가_하나면_그것을_반환() {
        Restaurant r1 = createRestaurant("kakao-1", "식당A", Category.KOREAN, "서울");
        given(restaurantRepository
                .findRecommended(Category.KOREAN, "서울", "강남구", "역삼동", null))
                .willReturn(List.of(r1));

        Restaurant result = restaurantService
                .recommend(Category.KOREAN, "서울", "강남구", "역삼동", null);

        assertThat(result).isEqualTo(r1);
    }

    @Test
    void recommend_조건에_맞는_식당_없으면_예외발생() {
        given(restaurantRepository
                .findRecommended(Category.CAFE, "부산", null, null, null))
                .willReturn(List.of());

        assertThatThrownBy(() -> restaurantService
                .recommend(Category.CAFE, "부산", null, null, null))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessage("조건에 맞는 식당이 없습니다.");
    }

    @Test
    void recommend_모든_필터_적용하여_추천() {
        Restaurant r1 = createRestaurant("kakao-1", "식당A", Category.JAPANESE, "서울");
        given(restaurantRepository
                .findRecommended(Category.JAPANESE, "서울", "강남구", null, null))
                .willReturn(List.of(r1));

        Restaurant result = restaurantService
                .recommend(Category.JAPANESE, "서울", "강남구", null, null);

        assertThat(result).isEqualTo(r1);
    }

    @Test
    void findByKakaoPlaceId_성공() {
        Restaurant restaurant = createRestaurant("kakao-1", "식당", Category.KOREAN, "서울");
        given(restaurantRepository.findByKakaoPlaceId("kakao-1")).willReturn(Optional.of(restaurant));

        Restaurant result = restaurantService.findByKakaoPlaceId("kakao-1");

        assertThat(result).isEqualTo(restaurant);
    }

    @Test
    void findByKakaoPlaceId_없으면_예외() {
        given(restaurantRepository.findByKakaoPlaceId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findByKakaoPlaceId("unknown"))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("DB에 없는 식당입니다.");
    }
}
