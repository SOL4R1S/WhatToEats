package com.whattoeat.domain.follow.controller;

import com.whattoeat.domain.user.entity.Provider;
import org.springframework.data.redis.core.RedisTemplate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.service.FollowService;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.AlreadyFollowingException;
import com.whattoeat.global.exception.FollowNotFoundException;
import com.whattoeat.global.exception.SelfFollowNotAllowedException;
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
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        controllers = FollowController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FollowControllerTest.AuthenticationPrincipalTestConfig.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FollowService followService;

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
    void follow_success() throws Exception {
        Follow follow = createFollow(10L, 1L, "me", "me.jpg", 2L, "target", "target.jpg");
        given(followService.follow(1L, 2L)).willReturn(follow);

        mockMvc.perform(post("/api/v1/follows/2").with(userDetails(1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("팔로우했습니다."))
                .andExpect(jsonPath("$.data.followId").value(10L))
                .andExpect(jsonPath("$.data.followerId").value(1L))
                .andExpect(jsonPath("$.data.followingId").value(2L))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void unfollow_success() throws Exception {
        willDoNothing().given(followService).unfollow(1L, 2L);

        mockMvc.perform(delete("/api/v1/follows/2").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("언팔로우했습니다."));
    }

    @Test
    void getFollowings_success() throws Exception {
        Follow follow = createFollow(10L, 1L, "me", "me.jpg", 2L, "target", "target.jpg");
        given(followService.getFollowings(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(follow)));
        given(followService.isFollowing(1L, 2L)).willReturn(true);

        mockMvc.perform(get("/api/v1/follows/followings").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(2L))
                .andExpect(jsonPath("$.data.content[0].nickname").value("target"))
                .andExpect(jsonPath("$.data.content[0].profileImage").value("target.jpg"))
                .andExpect(jsonPath("$.data.content[0].isFollowedByMe").value(true))
                .andExpect(jsonPath("$.data.content[0].createdAt").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getFollowers_success() throws Exception {
        Follow follow = createFollow(10L, 2L, "follower", "follower.jpg", 1L, "me", "me.jpg");
        given(followService.getFollowers(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(follow)));
        given(followService.isFollowing(1L, 2L)).willReturn(false);

        mockMvc.perform(get("/api/v1/follows/followers").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(2L))
                .andExpect(jsonPath("$.data.content[0].nickname").value("follower"))
                .andExpect(jsonPath("$.data.content[0].profileImage").value("follower.jpg"))
                .andExpect(jsonPath("$.data.content[0].isFollowedByMe").value(false))
                .andExpect(jsonPath("$.data.content[0].createdAt").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void follow_self_fails() throws Exception {
        given(followService.follow(1L, 1L))
                .willThrow(new SelfFollowNotAllowedException());

        mockMvc.perform(post("/api/v1/follows/1").with(userDetails(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신을 팔로우할 수 없습니다."));
    }

    @Test
    void follow_already_following_fails() throws Exception {
        given(followService.follow(1L, 2L))
                .willThrow(new AlreadyFollowingException());

        mockMvc.perform(post("/api/v1/follows/2").with(userDetails(1L)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 팔로우 중인 사용자입니다."));
    }

    @Test
    void unfollow_not_found_fails() throws Exception {
        org.mockito.BDDMockito.willThrow(new FollowNotFoundException())
                .given(followService).unfollow(1L, 2L);

        mockMvc.perform(delete("/api/v1/follows/2").with(userDetails(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("팔로우 관계가 존재하지 않습니다."));
    }

    @Test
    void follow_user_not_found_fails() throws Exception {
        given(followService.follow(1L, 999L))
                .willThrow(new UserNotFoundException(999L));

        mockMvc.perform(post("/api/v1/follows/999").with(userDetails(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }

    private RequestPostProcessor userDetails(Long userId) {
        return request -> {
            User user = createUser(userId, "user" + userId, "profile.jpg");
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setUserPrincipal(authentication);
            return request;
        };
    }

    private Follow createFollow(
            Long id,
            Long followerId,
            String followerNickname,
            String followerProfileImage,
            Long followingId,
            String followingNickname,
            String followingProfileImage) {
        Follow follow = Follow.of(
                createUser(followerId, followerNickname, followerProfileImage),
                createUser(followingId, followingNickname, followingProfileImage));
        ReflectionTestUtils.setField(follow, "id", id);
        ReflectionTestUtils.setField(follow, "createdAt", LocalDateTime.of(2026, 6, 30, 12, 0));
        return follow;
    }

    private User createUser(Long id, String nickname, String profileImage) {
        User user = User.builder()
                .loginId("user" + id)
                .password("password")
                .nickname(nickname)
                .profileImage(profileImage)
                .email("user" + id + "@test.com")
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2026, 6, 30, 12, 0));
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
