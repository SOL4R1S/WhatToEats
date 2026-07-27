package com.whattoeat.domain.restaurantlist.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurantlist.dto.RestaurantListRequest;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
import com.whattoeat.domain.restaurantlist.service.RestaurantListService;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.GlobalExceptionHandler;
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@WebMvcTest(
        controllers = RestaurantListController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantListItemControllerTest {

    private static final Long TEST_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private RestaurantListService restaurantListService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        restaurantListService = mock(RestaurantListService.class);
        userDetails = mock(CustomUserDetails.class);

        given(userDetails.getUserId())
                .willReturn(TEST_USER_ID);

        RestaurantListController controller =
                new RestaurantListController(restaurantListService);

        HandlerMethodArgumentResolver authenticationPrincipalResolver =
                new HandlerMethodArgumentResolver() {

                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(
                                AuthenticationPrincipal.class
                        );
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return userDetails;
                    }
                };

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(authenticationPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UsernamePasswordAuthenticationToken authenticationToken() {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of()
        );
    }

    private Restaurant mockRestaurant(Long id, String name, Category category) {
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(id);
        given(restaurant.getName()).willReturn(name);
        given(restaurant.getCategory()).willReturn(category);
        return restaurant;
    }

    private RestaurantList createRestaurantList(Long id) {
        User user = mock(User.class);

        RestaurantList restaurantList = new RestaurantList(
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        ReflectionTestUtils.setField(restaurantList, "id", id);
        ReflectionTestUtils.setField(restaurantList, "createdAt", LocalDateTime.now());

        return restaurantList;
    }

    private RestaurantListItem createRestaurantListItem(
            Long id,
            RestaurantList restaurantList,
            Restaurant restaurant,
            String memo,
            Integer orderIndex
    ) {
        RestaurantListItem item = new RestaurantListItem(
                restaurantList,
                restaurant,
                memo,
                orderIndex
        );

        ReflectionTestUtils.setField(item, "id", id);
        ReflectionTestUtils.setField(item, "createdAt", LocalDateTime.now());

        return item;
    }

    @Test
    void 맛집리스트_아이템_추가_성공() throws Exception {
        RestaurantListRequest.RestaurantListItem request =
                new RestaurantListRequest.RestaurantListItem(
                        10L,
                        "한줄평",
                        1
                );

        Restaurant restaurant = mockRestaurant(
                10L,
                "초밥집",
                Category.JAPANESE
        );

        RestaurantList restaurantList = createRestaurantList(1L);

        RestaurantListItem item = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        given(restaurantListService.addItem(
                eq(1L),
                eq(1L),
                eq(10L),
                eq("한줄평"),
                eq(1)
        )).willReturn(item);

        mockMvc.perform(post("/api/v1/lists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.restaurantId").value(10))
                .andExpect(jsonPath("$.data.restaurantName").value("초밥집"))
                .andExpect(jsonPath("$.data.category").value("JAPANESE"))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.memo").value("한줄평"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message").value("맛집 리스트에 식당이 추가되었습니다."));
    }

    @Test
    void 맛집리스트_아이템_수정_성공() throws Exception {
        // given
        RestaurantListRequest.RestaurantListItem request =
                new RestaurantListRequest.RestaurantListItem(
                        10L,
                        "수정된 한줄평",
                        2
                );

        Restaurant restaurant = mockRestaurant(
                10L,
                "초밥집",
                Category.JAPANESE
        );

        RestaurantList restaurantList = createRestaurantList(1L);

        RestaurantListItem item = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "수정된 한줄평",
                2
        );

        given(restaurantListService.updateItem(
                eq(TEST_USER_ID),
                eq(100L),
                eq(1L),
                eq(2),
                eq("수정된 한줄평")
        )).willReturn(item);

        // when & then
        mockMvc.perform(
                        put("/api/v1/lists/1/items/100")
                                .with(authentication(authenticationToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.restaurantId").value(10))
                .andExpect(jsonPath("$.data.restaurantName").value("초밥집"))
                .andExpect(jsonPath("$.data.category").value("JAPANESE"))
                .andExpect(jsonPath("$.data.orderIndex").value(2))
                .andExpect(jsonPath("$.data.memo").value("수정된 한줄평"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message")
                        .value("리스트 아이템 정보가 변경되었습니다."));

        verify(restaurantListService).updateItem(
                TEST_USER_ID,
                100L,
                1L,
                2,
                "수정된 한줄평"
        );
    }

    @Test
    void 맛집리스트_아이템_삭제_성공() throws Exception {
        // given
        Long listId = 1L;
        Long itemId = 100L;
        Long userId = 1L;

        willDoNothing()
                .given(restaurantListService)
                .deleteItem(
                        listId,
                        itemId,
                        userId
                );

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/v1/lists/{id}/items/{itemId}",
                                listId,
                                itemId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        then(restaurantListService)
                .should()
                .deleteItem(
                        listId,
                        itemId,
                        userId
                );
    }

    @Test
    void 맛집리스트_아이템_추가_restaurantId_null이면_400() throws Exception {
        RestaurantListRequest.RestaurantListItem request =
                new RestaurantListRequest.RestaurantListItem(
                        null,
                        "한줄평",
                        1
                );

        mockMvc.perform(post("/api/v1/lists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 맛집리스트_아이템_추가_orderIndex_null이면_맨뒤에_추가() throws Exception {
        RestaurantListRequest.RestaurantListItem request =
                new RestaurantListRequest.RestaurantListItem(
                        10L,
                        "한줄평",
                        null
                );

        Restaurant restaurant = mockRestaurant(
                10L,
                "초밥집",
                Category.JAPANESE
        );

        RestaurantList restaurantList = createRestaurantList(1L);

        RestaurantListItem item = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        given(restaurantListService.addItem(
                eq(1L),
                eq(1L),
                eq(10L),
                eq("한줄평"),
                isNull()
        )).willReturn(item);

        mockMvc.perform(post("/api/v1/lists/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.restaurantId").value(10))
                .andExpect(jsonPath("$.data.restaurantName").value("초밥집"))
                .andExpect(jsonPath("$.data.category").value("JAPANESE"))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.memo").value("한줄평"))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message").value("맛집 리스트에 식당이 추가되었습니다."));
    }
}
