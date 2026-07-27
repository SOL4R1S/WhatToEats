package com.whattoeat.domain.notification.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.notification.entity.Notification;
import com.whattoeat.domain.notification.entity.NotificationType;
import com.whattoeat.domain.notification.service.NotificationService;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.exception.NotificationNotFoundException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        controllers = NotificationController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(NotificationControllerTest.AuthenticationPrincipalTestConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

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

    private User createUser(Long id, String nickname) {
        User user = User.builder().nickname(nickname).email(nickname + "@test.com").provider(Provider.LOCAL).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private RequestPostProcessor userDetails(Long userId) {
        return request -> {
            User user = createUser(userId, "user" + userId);
            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setUserPrincipal(authentication);
            return request;
        };
    }

    private Notification createNotification(Long id, User receiver, User actor, Feed feed) {
        Notification notification = Notification.of(
                receiver, actor, feed, NotificationType.NEW_FEED, actor.getNickname() + "님이 새 글을 작성했습니다."
        );
        ReflectionTestUtils.setField(notification, "id", id);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());
        return notification;
    }

    @Test
    void 알림_목록_조회_성공() throws Exception {
        User receiver = createUser(1L, "받는사람");
        User actor = createUser(2L, "작성자");
        Feed feed = Feed.builder().user(actor).content("맛집").build();
        ReflectionTestUtils.setField(feed, "id", 10L);

        Notification notification = createNotification(100L, receiver, actor, feed);
        Pageable pageable = PageRequest.of(0, 20);

        given(notificationService.getNotifications(eq(1L), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(notification), pageable, 1));

        mockMvc.perform(get("/api/v1/notifications").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].actorId").value(2))
                .andExpect(jsonPath("$.data[0].actorNickname").value("작성자"))
                .andExpect(jsonPath("$.data[0].feedId").value(10))
                .andExpect(jsonPath("$.data[0].type").value("NEW_FEED"))
                .andExpect(jsonPath("$.data[0].isRead").value(false));
    }

    @Test
    void 알림_읽음_처리_성공() throws Exception {
        User receiver = createUser(1L, "받는사람");
        User actor = createUser(2L, "작성자");
        Feed feed = Feed.builder().user(actor).content("맛집").build();
        ReflectionTestUtils.setField(feed, "id", 10L);

        Notification notification = createNotification(100L, receiver, actor, feed);
        notification.markAsRead();

        given(notificationService.markAsRead(1L, 100L)).willReturn(notification);

        mockMvc.perform(put("/api/v1/notifications/100/read").with(userDetails(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isRead").value(true))
                .andExpect(jsonPath("$.message").value("알림을 읽음 처리했습니다."));
    }

    @Test
    void 알림_읽음_처리_존재하지않으면_404() throws Exception {
        given(notificationService.markAsRead(1L, 999L)).willThrow(new NotificationNotFoundException(999L));

        mockMvc.perform(put("/api/v1/notifications/999/read").with(userDetails(1L)))
                .andExpect(status().isNotFound());
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
