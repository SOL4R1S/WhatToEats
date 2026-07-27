package com.whattoeat.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.whattoeat.domain.comment.dto.CommentRequest;
import com.whattoeat.domain.comment.dto.CommentResponse;
import com.whattoeat.domain.comment.entity.Comment;
import com.whattoeat.domain.comment.repository.CommentRepository;
import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.CommentNotFoundException;
import com.whattoeat.global.exception.FeedNotFoundException;
import com.whattoeat.global.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User createUser(String nickname) {
        return User.builder()
                .loginId("testuser")
                .nickname(nickname)
                .email("test@example.com")
                .provider(Provider.LOCAL)
                .build();
    }

    private Feed createFeed(User user) {
        return Feed.builder()
                .user(user)
                .content("피드 내용")
                .build();
    }

    @Test
    void getComments_성공() {
        User user = createUser("testUser");
        Feed feed = createFeed(user);
        Comment comment1 = Comment.builder().feed(feed).user(user).content("댓글1").build();
        Comment comment2 = Comment.builder().feed(feed).user(user).content("댓글2").build();
        given(commentRepository.findByFeedId(1L)).willReturn(List.of(comment1, comment2));

        List<CommentResponse> result = commentService.getComments(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("댓글1");
        assertThat(result.get(1).content()).isEqualTo("댓글2");
    }

    @Test
    void getComments_댓글이_없으면_빈_리스트_반환() {
        given(commentRepository.findByFeedId(1L)).willReturn(List.of());

        List<CommentResponse> result = commentService.getComments(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void createComment_성공() {
        User user = createUser("testUser");
        Feed feed = createFeed(user);
        CommentRequest request = new CommentRequest("새 댓글");
        Comment saved = Comment.builder().feed(feed).user(user).content("새 댓글").build();
        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(commentRepository.save(any())).willReturn(saved);

        CommentResponse result = commentService.createComment(1L, 1L, request);

        assertThat(result.content()).isEqualTo("새 댓글");
        assertThat(result.nickname()).isEqualTo("testUser");
    }

    @Test
    void createComment_피드가_없으면_예외발생() {
        given(feedRepository.findById(999L)).willReturn(Optional.empty());
        CommentRequest request = new CommentRequest("댓글");

        assertThatThrownBy(() -> commentService.createComment(999L, 1L, request))
                .isInstanceOf(FeedNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void createComment_유저가_없으면_예외발생() {
        User user = createUser("testUser");
        Feed feed = createFeed(user);
        given(feedRepository.findById(1L)).willReturn(Optional.of(feed));
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        CommentRequest request = new CommentRequest("댓글");

        assertThatThrownBy(() -> commentService.createComment(1L, 999L, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteComment_성공() {
        User user = createUser("testUser");
        Feed feed = createFeed(user);
        Comment comment = Comment.builder().feed(feed).user(user).content("댓글").build();
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        commentService.deleteComment(feed.getId(), 1L);

        then(commentRepository).should().delete(comment);
    }

    @Test
    void deleteComment_댓글이_없으면_예외발생() {
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 999L))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteComment_다른_피드의_댓글이면_예외발생() {
        User user = createUser("testUser");
        Feed feed = createFeed(user);
        Comment comment = Comment.builder().feed(feed).user(user).content("댓글").build();
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
