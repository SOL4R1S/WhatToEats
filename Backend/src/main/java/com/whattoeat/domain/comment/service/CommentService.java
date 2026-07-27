package com.whattoeat.domain.comment.service;

import com.whattoeat.domain.comment.dto.CommentRequest;
import com.whattoeat.domain.comment.dto.CommentResponse;
import com.whattoeat.domain.comment.entity.Comment;
import com.whattoeat.domain.comment.repository.CommentRepository;
import com.whattoeat.domain.feed.entity.Feed;
import com.whattoeat.domain.feed.repository.FeedRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.CommentNotFoundException;
import com.whattoeat.global.exception.FeedNotFoundException;
import com.whattoeat.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getComments(Long feedId) {
        return commentRepository.findByFeedId(feedId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long feedId, Long userId, CommentRequest request) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new FeedNotFoundException(feedId));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Comment comment = Comment.builder()
                .feed(feed)
                .user(user)
                .content(request.content())
                .build();
        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long feedId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        if (!Objects.equals(comment.getFeed().getId(), feedId)) {
            throw new IllegalArgumentException("해당 피드의 댓글이 아닙니다.");
        }
        commentRepository.delete(comment);
    }
}
