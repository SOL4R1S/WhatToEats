package com.whattoeat.domain.feedlike.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whattoeat.domain.feedlike.dto.FeedLikeResponse;
import com.whattoeat.domain.feedlike.service.FeedLikeService;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.AlreadyLikedFeedException;
import com.whattoeat.global.exception.FeedNotFoundException;
import com.whattoeat.global.exception.FeedLikeNotFoundException;
import com.whattoeat.global.exception.UserNotFoundException;
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.security.CustomUserDetailsService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(
        controllers = FeedLikeController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FeedLikeControllerTest.AuthenticationPrincipalTestConfig.class)
class FeedLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedLikeService feedLikeService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void like_success() throws Exception {
        FeedLikeResponse response = FeedLikeResponse.of(2L, 1, true);
        given(feedLikeService.like(1L, 2L)).willReturn(response);

        mockMvc.perform(post("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("좋아요를 눌렀습니다."))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.isLikedByMe").value(true));
    }

    @Test
    void unlike_success() throws Exception {
        FeedLikeResponse response = FeedLikeResponse.of(2L, 0, false);
        given(feedLikeService.unlike(1L, 2L)).willReturn(response);

        mockMvc.perform(delete("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("좋아요를 취소했습니다."))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.isLikedByMe").value(false));
    }

    @Test
    void isLiked_true() throws Exception {
        FeedLikeResponse response = FeedLikeResponse.of(2L, 1, true);
        given(feedLikeService.getLikeStatus(1L, 2L)).willReturn(response);

        mockMvc.perform(get("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.likeCount").value(1))
                .andExpect(jsonPath("$.data.isLikedByMe").value(true));
    }

    @Test
    void isLiked_false() throws Exception {
        FeedLikeResponse response = FeedLikeResponse.of(2L, 0, false);
        given(feedLikeService.getLikeStatus(1L, 2L)).willReturn(response);

        mockMvc.perform(get("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedId").value(2L))
                .andExpect(jsonPath("$.data.likeCount").value(0))
                .andExpect(jsonPath("$.data.isLikedByMe").value(false));
    }

    @Test
    void like_already_liked_fails() throws Exception {
        given(feedLikeService.like(1L, 2L))
                .willThrow(new AlreadyLikedFeedException());

        mockMvc.perform(post("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 좋아요한 피드입니다."));
    }

    @Test
    void unlike_not_found_fails() throws Exception {
        org.mockito.BDDMockito.willThrow(new FeedLikeNotFoundException())
                .given(feedLikeService).unlike(1L, 2L);

        mockMvc.perform(delete("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("좋아요 관계가 존재하지 않습니다."));
    }

    @Test
    void like_user_not_found_fails() throws Exception {
        given(feedLikeService.like(1L, 2L))
                .willThrow(new UserNotFoundException(1L));

        mockMvc.perform(post("/api/v1/feeds/2/like").with(userDetails(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 1"));
    }

    @Test
    void like_feed_not_found_fails() throws Exception {
        given(feedLikeService.like(1L, 999L))
                .willThrow(new FeedNotFoundException(999L));

        mockMvc.perform(post("/api/v1/feeds/999/like").with(userDetails(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Feed not found: 999"));
    }

    private RequestPostProcessor userDetails(Long userId) {
        return request -> {
            User user = createUser(userId);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setUserPrincipal(authentication);
            return request;
        };
    }



    private User createUser(Long id) {
        User user = User.builder()
                .loginId("user" + id)
                .password("password")
                .nickname("user" + id)
                .profileImage("profile.jpg")
                .email("user" + id + "@test.com")
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2026, 7, 2, 12, 0));
        return user;
    }

    @TestConfiguration
    static class AuthenticationPrincipalTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                            && CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
                }

                @Override
                public Object resolveArgument(
                        MethodParameter parameter,
                        ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest,
                        WebDataBinderFactory binderFactory) {
                    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                }
            });
        }
    }
}
