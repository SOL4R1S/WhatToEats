package com.whattoeat.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.whattoeat.domain.comment.dto.CommentRequest;
import com.whattoeat.domain.comment.dto.CommentResponse;
import com.whattoeat.domain.comment.service.CommentService;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.jwt.JwtUtil;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = CommentController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setupSecurityContext() {
        User user = User.builder()
                .nickname("testUser")
                .email("test@test.com")
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user,"id",1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CommentResponse createResponse(Long id, String content, Long userId, String nickname) {
        return new CommentResponse(id, content, userId, nickname, LocalDateTime.now());
    }

    @Test
    void getComments_성공() throws Exception {
        List<CommentResponse> responses = List.of(
                createResponse(1L, "첫 번째 댓글", 1L, "user1"),
                createResponse(2L, "두 번째 댓글", 2L, "user2")
        );
        given(commentService.getComments(1L)).willReturn(responses);

        mockMvc.perform(get("/api/v1/feeds/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 목록 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value("첫 번째 댓글"))
                .andExpect(jsonPath("$.data[1].content").value("두 번째 댓글"));
    }

    @Test
    void getComments_댓글이_없으면_빈_배열_반환() throws Exception {
        given(commentService.getComments(1L)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/feeds/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void createComment_성공() throws Exception {
        CommentRequest request = new CommentRequest("새 댓글");
        CommentResponse response = createResponse(1L, "새 댓글", 1L, "user1");
        given(commentService.createComment(eq(1L), eq(1L), any(CommentRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/feeds/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 작성 성공"))
                .andExpect(jsonPath("$.data.content").value("새 댓글"))
                .andExpect(jsonPath("$.data.nickname").value("user1"));
    }

    @Test
    void createComment_content가_blank이면_400() throws Exception {
        CommentRequest request = new CommentRequest("");

        mockMvc.perform(post("/api/v1/feeds/1/comments")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_content가_500자_초과이면_400() throws Exception {
        CommentRequest request = new CommentRequest("a".repeat(501));

        mockMvc.perform(post("/api/v1/feeds/1/comments")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_성공() throws Exception {
        willDoNothing().given(commentService).deleteComment(1L, 1L);

        mockMvc.perform(delete("/api/v1/feeds/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

    }
}
