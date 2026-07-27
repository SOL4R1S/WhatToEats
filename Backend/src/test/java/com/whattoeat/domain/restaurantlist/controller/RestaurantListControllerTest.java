package com.whattoeat.domain.restaurantlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.security.CustomUserDetailsService;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.context.SecurityContextHolder;

@WebMvcTest(
        controllers = RestaurantListController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantListControllerTest {

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

    private static final Long TEST_USER_ID = 1L;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = mock(CustomUserDetails.class);

        given(userDetails.getUserId())
                .willReturn(TEST_USER_ID);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User mockUser(Long id, String nickname) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getNickname()).willReturn(nickname);
        return user;
    }

    private Restaurant mockRestaurant(Long id, String name, Category category) {
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.getId()).willReturn(id);
        given(restaurant.getName()).willReturn(name);
        given(restaurant.getCategory()).willReturn(category);
        return restaurant;
    }

    private RestaurantList createRestaurantList(
            Long id,
            User user,
            String title,
            String description,
            MoodTag moodTag
    ) {
        RestaurantList restaurantList = new RestaurantList(
                user,
                title,
                description,
                moodTag
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
    void 맛집리스트_생성_성공() throws Exception {
        RestaurantListRequest.RestaurantList request =
                new RestaurantListRequest.RestaurantList(
                        "데이트 맛집",
                        "분위기 좋은 곳",
                        MoodTag.DATE
                );

        User user = mockUser(1L, "user1");

        RestaurantList restaurantList = createRestaurantList(
                1L,
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        given(restaurantListService.create(
                eq(1L),
                eq("데이트 맛집"),
                eq("분위기 좋은 곳"),
                eq(MoodTag.DATE)
        )).willReturn(restaurantList);

        mockMvc.perform(post("/api/v1/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("user1"))
                .andExpect(jsonPath("$.data.title").value("데이트 맛집"))
                .andExpect(jsonPath("$.data.description").value("분위기 좋은 곳"))
                .andExpect(jsonPath("$.data.moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.itemCount").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message").value("맛집 리스트가 생성되었습니다."));
    }

    @Test
    void 맛집리스트_생성_title_blank면_400() throws Exception {
        RestaurantListRequest.RestaurantList request =
                new RestaurantListRequest.RestaurantList(
                        "",
                        "설명",
                        MoodTag.DATE
                );

        mockMvc.perform(post("/api/v1/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 내_맛집리스트_다건조회_성공() throws Exception {
        User user = mockUser(1L, "user1");

        RestaurantList list1 = createRestaurantList(
                1L,
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        RestaurantList list2 = createRestaurantList(
                2L,
                user,
                "혼밥 맛집",
                "혼자 먹기 좋은 곳",
                MoodTag.SOLO
        );

        given(restaurantListService.findAllByUserId(
                eq(1L),
                any(Pageable.class)
        )).willReturn(
                new PageImpl<>(
                        List.of(list2, list1),
                        PageRequest.of(0, 10),
                        2
                )
        );

        mockMvc.perform(get("/api/v1/lists")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))

                // 페이징 응답 구조
                .andExpect(jsonPath("$.data.lists.length()").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))

                // 첫 번째 리스트
                .andExpect(jsonPath("$.data.lists[0].id").value(2))
                .andExpect(jsonPath("$.data.lists[0].userId").value(1))
                .andExpect(jsonPath("$.data.lists[0].nickname").value("user1"))
                .andExpect(jsonPath("$.data.lists[0].title").value("혼밥 맛집"))
                .andExpect(jsonPath("$.data.lists[0].description").value("혼자 먹기 좋은 곳"))
                .andExpect(jsonPath("$.data.lists[0].moodTag").value("SOLO"))
                .andExpect(jsonPath("$.data.lists[0].itemCount").value(0))
                .andExpect(jsonPath("$.data.lists[0].createdAt").exists())

                // 두 번째 리스트
                .andExpect(jsonPath("$.data.lists[1].id").value(1))
                .andExpect(jsonPath("$.data.lists[1].title").value("데이트 맛집"))
                .andExpect(jsonPath("$.data.lists[1].moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.lists[1].itemCount").value(0))
                .andExpect(jsonPath("$.data.lists[1].createdAt").exists())

                .andExpect(jsonPath("$.message")
                        .value("맛집 리스트 목록 조회가 완료되었습니다."));
    }

    @Test
    void 내_맛집리스트_단건조회_성공() throws Exception {
        User user = mockUser(1L, "user1");

        Restaurant restaurant = mockRestaurant(
                10L,
                "초밥집",
                Category.JAPANESE
        );

        RestaurantList restaurantList = createRestaurantList(
                1L,
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        RestaurantListItem item = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        restaurantList.getItems().add(item);

        given(restaurantListService.findByIdAndUserId(1L, 1L))
                .willReturn(restaurantList);

        mockMvc.perform(get("/api/v1/lists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("user1"))
                .andExpect(jsonPath("$.data.title").value("데이트 맛집"))
                .andExpect(jsonPath("$.data.description").value("분위기 좋은 곳"))
                .andExpect(jsonPath("$.data.moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(100))
                .andExpect(jsonPath("$.data.items[0].listId").value(1))
                .andExpect(jsonPath("$.data.items[0].restaurantId").value(10))
                .andExpect(jsonPath("$.data.items[0].restaurantName").value("초밥집"))
                .andExpect(jsonPath("$.data.items[0].category").value("JAPANESE"))
                .andExpect(jsonPath("$.data.items[0].orderIndex").value(1))
                .andExpect(jsonPath("$.data.items[0].memo").value("한줄평"))
                .andExpect(jsonPath("$.data.items[0].createdAt").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message").value("맛집 리스트 조회가 완료되었습니다."));
    }

    @Test
    void 전체_맛집리스트_다건조회_성공() throws Exception {
        User user = mockUser(1L, "user1");

        RestaurantList list1 = createRestaurantList(
                1L,
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        RestaurantList list2 = createRestaurantList(
                2L,
                user,
                "혼밥 맛집",
                "혼자 먹기 좋은 곳",
                MoodTag.SOLO
        );

        Pageable pageable = PageRequest.of(0, 10);

        given(restaurantListService.findAll(any(Pageable.class)))
                .willReturn(
                        new PageImpl<>(
                                List.of(list2, list1),
                                pageable,
                                2
                        )
                );

        mockMvc.perform(get("/api/v1/lists/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))

                // 페이징 응답
                .andExpect(jsonPath("$.data.lists.length()").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))

                // 첫 번째 리스트
                .andExpect(jsonPath("$.data.lists[0].id").value(2))
                .andExpect(jsonPath("$.data.lists[0].userId").value(1))
                .andExpect(jsonPath("$.data.lists[0].nickname").value("user1"))
                .andExpect(jsonPath("$.data.lists[0].title").value("혼밥 맛집"))
                .andExpect(jsonPath("$.data.lists[0].description").value("혼자 먹기 좋은 곳"))
                .andExpect(jsonPath("$.data.lists[0].moodTag").value("SOLO"))
                .andExpect(jsonPath("$.data.lists[0].itemCount").value(0))
                .andExpect(jsonPath("$.data.lists[0].createdAt").exists())

                // 두 번째 리스트
                .andExpect(jsonPath("$.data.lists[1].id").value(1))
                .andExpect(jsonPath("$.data.lists[1].userId").value(1))
                .andExpect(jsonPath("$.data.lists[1].nickname").value("user1"))
                .andExpect(jsonPath("$.data.lists[1].title").value("데이트 맛집"))
                .andExpect(jsonPath("$.data.lists[1].description").value("분위기 좋은 곳"))
                .andExpect(jsonPath("$.data.lists[1].moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.lists[1].itemCount").value(0))
                .andExpect(jsonPath("$.data.lists[1].createdAt").exists())

                .andExpect(jsonPath("$.message")
                        .value("전체 맛집 리스트 목록 조회가 완료되었습니다."));
    }

    @Test
    void 전체_맛집리스트_단건조회_성공() throws Exception {
        User user = mockUser(1L, "user1");

        RestaurantList restaurantList = createRestaurantList(
                1L,
                user,
                "데이트 맛집",
                "분위기 좋은 곳",
                MoodTag.DATE
        );

        given(restaurantListService.findById(1L))
                .willReturn(restaurantList);

        mockMvc.perform(get("/api/v1/lists/all/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("user1"))
                .andExpect(jsonPath("$.data.title").value("데이트 맛집"))
                .andExpect(jsonPath("$.data.description").value("분위기 좋은 곳"))
                .andExpect(jsonPath("$.data.moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.items.length()").value(0))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.message").value("전체 맛집 리스트 조회가 완료되었습니다."));
    }

    @Test
    void copyRestaurantList_성공() throws Exception {
        // given
        Long originalListId = 1L;
        Long userId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        given(user.getNickname()).willReturn("user1");

        RestaurantList copiedList = mock(RestaurantList.class);
        given(copiedList.getId()).willReturn(2L);
        given(copiedList.getUser()).willReturn(user);
        given(copiedList.getTitle()).willReturn("혼밥 맛집");
        given(copiedList.getDescription()).willReturn("혼자 먹기 좋은 곳");
        given(copiedList.getMoodTag()).willReturn(MoodTag.SOLO);
        given(copiedList.getItems()).willReturn(List.of());

        // ⭐ 이거 추가
        given(copiedList.getCreatedAt())
                .willReturn(LocalDateTime.of(2026, 7, 5, 10, 0));

        given(restaurantListService.copyList(userId, originalListId))
                .willReturn(copiedList);

        // when & then
        mockMvc.perform(
                        post("/api/v1/lists/{id}/copy", originalListId)
                                .with(authentication(
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                List.of()
                                        )
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("맛집 리스트가 복사되었습니다."));

        verify(restaurantListService)
                .copyList(userId, originalListId);
    }

    @Test
    void 식당_리스트_기본_정보_수정_성공() throws Exception {
        // given
        Long listId = 1L;
        Long userId = 1L;

        RestaurantList restaurantList = mock(RestaurantList.class);
        User user = mock(User.class);

        given(restaurantList.getId())
                .willReturn(listId);

        given(restaurantList.getUser())
                .willReturn(user);

        given(user.getId())
                .willReturn(userId);

        given(user.getNickname())
                .willReturn("푸디");

        given(restaurantList.getTitle())
                .willReturn("수정된 리스트 제목");

        given(restaurantList.getDescription())
                .willReturn("수정된 리스트 설명");

        given(restaurantList.getMoodTag())
                .willReturn(MoodTag.DATE);

        given(restaurantList.getItems())
                .willReturn(List.of());

        given(restaurantListService.update(
                listId,
                userId,
                "수정된 리스트 제목",
                "수정된 리스트 설명",
                MoodTag.DATE
        )).willReturn(restaurantList);

        String requestBody = """
            {
                "title": "수정된 리스트 제목",
                "description": "수정된 리스트 설명",
                "moodTag": "DATE"
            }
            """;

        // when & then
        mockMvc.perform(
                        put("/api/v1/lists/{id}", listId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("리스트 정보가 변경되었습니다."))
                .andExpect(jsonPath("$.data.listId").value(1))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("푸디"))
                .andExpect(jsonPath("$.data.title")
                        .value("수정된 리스트 제목"))
                .andExpect(jsonPath("$.data.description")
                        .value("수정된 리스트 설명"))
                .andExpect(jsonPath("$.data.moodTag").value("DATE"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty());

        then(restaurantListService)
                .should()
                .update(
                        listId,
                        userId,
                        "수정된 리스트 제목",
                        "수정된 리스트 설명",
                        MoodTag.DATE
                );
    }
}
