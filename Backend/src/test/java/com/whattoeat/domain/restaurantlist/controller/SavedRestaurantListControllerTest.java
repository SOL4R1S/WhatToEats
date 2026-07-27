package com.whattoeat.domain.restaurantlist.controller;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurantlist.dto.SavedRestaurantListResponse;
import com.whattoeat.domain.restaurantlist.service.SavedRestaurantListService;
import com.whattoeat.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

class SavedRestaurantListControllerTest {

    private static final Long TEST_USER_ID = 2L;

    private MockMvc mockMvc;

    private SavedRestaurantListService savedRestaurantListService;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        savedRestaurantListService =
                Mockito.mock(SavedRestaurantListService.class);

        userDetails = Mockito.mock(CustomUserDetails.class);

        given(userDetails.getUserId())
                .willReturn(TEST_USER_ID);

        SavedRestaurantListController controller =
                new SavedRestaurantListController(savedRestaurantListService);

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
                .setCustomArgumentResolvers(
                        authenticationPrincipalResolver,
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void save_성공() throws Exception {
        // given
        Long restaurantListId = 10L;

        willDoNothing()
                .given(savedRestaurantListService)
                .save(TEST_USER_ID, restaurantListId);

        // when & then
        mockMvc.perform(post("/api/v1/restaurant_lists/{restaurantListId}/save", restaurantListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("레스토랑 리스트를 저장했습니다."));

        then(savedRestaurantListService)
                .should()
                .save(TEST_USER_ID, restaurantListId);
    }

    @Test
    void unsave_성공() throws Exception {
        // given
        Long restaurantListId = 10L;

        willDoNothing()
                .given(savedRestaurantListService)
                .unsave(TEST_USER_ID, restaurantListId);

        // when & then
        mockMvc.perform(delete("/api/v1/restaurant_lists/{restaurantListId}/save", restaurantListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("레스토랑 리스트 저장을 취소했습니다."));

        then(savedRestaurantListService)
                .should()
                .unsave(TEST_USER_ID, restaurantListId);
    }

    @Test
    void findMySavedLists_성공() throws Exception {
        // given
        SavedRestaurantListResponse response = new SavedRestaurantListResponse(
                10L,                                     // listId
                2L,                                      // userId
                "작성자",                                 // nickname
                "혼밥 맛집",                              // title
                "혼자 먹기 좋은 곳",                       // description
                MoodTag.SOLO,                            // moodTag
                List.of(),                               // items
                LocalDateTime.of(2026, 7, 4, 2, 30)     // savedAt
        );

        Page<SavedRestaurantListResponse> page =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        given(savedRestaurantListService.findMySavedLists(eq(TEST_USER_ID), any()))
                .willReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/restaurant_lists/saved")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("저장한 레스토랑 리스트 조회가 완료되었습니다."))

                .andExpect(jsonPath("$.data.content[0].listId").value(10L))
                .andExpect(jsonPath("$.data.content[0].userId").value(2L))
                .andExpect(jsonPath("$.data.content[0].nickname").value("작성자"))
                .andExpect(jsonPath("$.data.content[0].title").value("혼밥 맛집"))
                .andExpect(jsonPath("$.data.content[0].description").value("혼자 먹기 좋은 곳"))
                .andExpect(jsonPath("$.data.content[0].moodTag").value("SOLO"))
                .andExpect(jsonPath("$.data.content[0].items").isArray())
                .andExpect(jsonPath("$.data.content[0].items").isEmpty())
                .andExpect(jsonPath("$.data.content[0].savedAt")
                        .value("2026-07-04T02:30:00"));

        then(savedRestaurantListService)
                .should()
                .findMySavedLists(eq(TEST_USER_ID), any());
    }

    @Test
    void isSaved_저장되어있으면_true() throws Exception {
        // given
        Long restaurantListId = 10L;

        given(savedRestaurantListService.isSaved(TEST_USER_ID, restaurantListId))
                .willReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/restaurant_lists/{restaurantListId}/saved", restaurantListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.restaurantListId").value(10L))
                .andExpect(jsonPath("$.data.saved").value(true))
                .andExpect(jsonPath("$.message").value("저장한 레스토랑 리스트입니다."));

        then(savedRestaurantListService)
                .should()
                .isSaved(TEST_USER_ID, restaurantListId);
    }

    @Test
    void isSaved_저장되어있지않으면_false() throws Exception {
        // given
        Long restaurantListId = 10L;

        given(savedRestaurantListService.isSaved(TEST_USER_ID, restaurantListId))
                .willReturn(false);

        // when & then
        mockMvc.perform(get("/api/v1/restaurant_lists/{restaurantListId}/saved", restaurantListId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.restaurantListId").value(10L))
                .andExpect(jsonPath("$.data.saved").value(false))
                .andExpect(jsonPath("$.message").value("저장하지 않은 레스토랑 리스트입니다."));

        then(savedRestaurantListService)
                .should()
                .isSaved(TEST_USER_ID, restaurantListId);
    }
}
