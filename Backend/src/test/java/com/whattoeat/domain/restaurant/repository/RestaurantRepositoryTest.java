package com.whattoeat.domain.restaurant.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.global.config.JpaConfig;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Restaurant koreanSeoul;
    private Restaurant westernSeoul;
    private Restaurant koreanBusan;

    @BeforeEach
    void setUp() {
        koreanSeoul = entityManager.persistAndFlush(new Restaurant(
                "kakao-1", "서울한식당", Category.KOREAN,
                "서울시 강남구", "서울시 강남구 테헤란로", "서울", "강남구", "역삼동", null,
                "02-1111-1111", 37.5, 127.0));

        westernSeoul = entityManager.persistAndFlush(new Restaurant(
                "kakao-2", "서울양식당", Category.WESTERN,
                "서울시 서초구", "서울시 서초구 서초대로", "서울", "서초구", "서초동", null,
                "02-2222-2222", 37.5, 127.1));

        koreanBusan = entityManager.persistAndFlush(new Restaurant(
                "kakao-3", "부산한식당", Category.KOREAN,
                "부산시 해운대구", "부산시 해운대구 해운대로", "부산", "해운대구", "우동", null,
                "051-3333-3333", 35.1, 129.1));
    }

    @Test
    void findRecommended_필터없이_전체_조회() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(null, null, null, null, null);

        assertThat(result).hasSize(3);
    }

    @Test
    void findRecommended_카테고리_필터만_적용() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(Category.KOREAN, null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("category").containsOnly(Category.KOREAN);
    }

    @Test
    void findRecommended_지역1_필터만_적용() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(null, "서울", null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting("region1").containsOnly("서울");
    }

    @Test
    void findRecommended_카테고리와_지역1_필터_조합() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(Category.KOREAN, "서울", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("서울한식당");
    }

    @Test
    void findRecommended_모든_필터_조합() {
        List<Restaurant> result = restaurantRepository.findRecommended(
                Category.KOREAN, "서울", "강남구", "역삼동", null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKakaoPlaceId()).isEqualTo("kakao-1");
    }

    @Test
    void findRecommended_조건에_맞는_데이터_없으면_빈_리스트() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(Category.CAFE, null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void findRecommended_지역2_필터만_적용() {
        List<Restaurant> result = restaurantRepository
                .findRecommended(null, null, "강남구", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRegion2()).isEqualTo("강남구");
    }

    @Test
    void findByKakaoPlaceId_성공() {
        Optional<Restaurant> result = restaurantRepository.findByKakaoPlaceId("kakao-1");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("서울한식당");
    }

    @Test
    void findByKakaoPlaceId_없으면_empty() {
        Optional<Restaurant> result = restaurantRepository.findByKakaoPlaceId("unknown");
        assertThat(result).isEmpty();
    }
}
