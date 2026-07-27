package com.whattoeat.domain.restaurant.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.service.RestaurantService;
import com.whattoeat.global.exception.RestaurantNotFoundException;
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.security.CustomUserDetailsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = RestaurantController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    private Restaurant createRestaurant(String kakaoId, String name, Category category) {
        return new Restaurant(
                kakaoId,
                name,
                category,
                "서울시 강남구",
                "서울시 강남구 테헤란로",
                "서울",
                "강남구",
                "역삼동",
                null,
                "02-0000-0000",
                37.5,
                127.0
        );
    }

    @Test
    void recommend_성공() throws Exception {
        Restaurant restaurant = createRestaurant("kakao-1", "맛있는식당", Category.KOREAN);
        given(restaurantService.recommend(null, null, null, null, null)).willReturn(restaurant);

        mockMvc.perform(get("/api/v1/restaurants/recommend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("맛있는식당"))
                .andExpect(jsonPath("$.data.category").value("KOREAN"))
                .andExpect(jsonPath("$.message").value("식당 추천이 완료되었습니다."));
    }

    @Test
    void recommend_카테고리_파라미터로_조회() throws Exception {
        Restaurant restaurant = createRestaurant("kakao-2", "한식당", Category.KOREAN);
        given(restaurantService.recommend(Category.KOREAN, null, null, null, null)).willReturn(restaurant);

        mockMvc.perform(get("/api/v1/restaurants/recommend")
                        .param("category", "KOREAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.category").value("KOREAN"));
    }

    @Test
    void recommend_지역_파라미터로_조회() throws Exception {
        Restaurant restaurant = createRestaurant("kakao-3", "서울식당", Category.WESTERN);
        given(restaurantService.recommend(null, "서울", "강남구", null, null)).willReturn(restaurant);

        mockMvc.perform(get("/api/v1/restaurants/recommend")
                        .param("region1", "서울")
                        .param("region2", "강남구"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.region1").value("서울"))
                .andExpect(jsonPath("$.data.region2").value("강남구"));
    }

    @Test
    void recommend_조건에_맞는_식당_없으면_404() throws Exception {
        given(restaurantService.recommend(Category.CAFE, "제주", null, null, null))
                .willThrow(new RestaurantNotFoundException("조건에 맞는 식당이 없습니다."));

        mockMvc.perform(get("/api/v1/restaurants/recommend")
                        .param("category", "CAFE")
                        .param("region1", "제주"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("조건에 맞는 식당이 없습니다."));
    }

    @Test
    void recommend_잘못된_카테고리_파라미터_400() throws Exception {
        mockMvc.perform(get("/api/v1/restaurants/recommend")
                        .param("category", "INVALID_CATEGORY"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("kakaoPlaceId로 조회 시 DB에 있으면 200 반환")
    void getByKakaoPlaceId_성공() throws Exception {
        Restaurant restaurant = createRestaurant("kakao-1", "맛집", Category.KOREAN);
        given(restaurantService.findByKakaoPlaceId("kakao-1")).willReturn(restaurant);

        mockMvc.perform(get("/api/v1/restaurants")
                        .param("kakaoPlaceId", "kakao-1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("맛집"))
                .andExpect(jsonPath("$.data.kakaoPlaceId").value("kakao-1"));
    }

    @Test
    @DisplayName("kakaoPlaceId로 조회 시 DB에 없으면 404 반환")
    void getByKakaoPlaceId_없음_404() throws Exception {
        given(restaurantService.findByKakaoPlaceId("unknown"))
                .willThrow(new RestaurantNotFoundException("DB에 없는 식당입니다."));

        mockMvc.perform(get("/api/v1/restaurants")
                        .param("kakaoPlaceId", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하는 id로 식당 상세 조회 시 200과 식당 정보를 반환한다")
    void getRestaurantById_성공() throws Exception {
        Restaurant restaurant = createRestaurant("kakao-99", "을지삼겹", Category.KOREAN);
        given(restaurantService.findById(1L)).willReturn(restaurant);

        mockMvc.perform(get("/api/v1/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("을지삼겹"))
                .andExpect(jsonPath("$.data.category").value("KOREAN"))
                .andExpect(jsonPath("$.message").value("식당 조회가 완료 되었습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 id로 식당 상세 조회 시 404를 반환한다")
    void getRestaurantById_존재하지_않는_id_404() throws Exception {
        given(restaurantService.findById(999L))
                .willThrow(new RestaurantNotFoundException(999L));

        mockMvc.perform(get("/api/v1/restaurants/999"))
                .andExpect(status().isNotFound());
    }
}
