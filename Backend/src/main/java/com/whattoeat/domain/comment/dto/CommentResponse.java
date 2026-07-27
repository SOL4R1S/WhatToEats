package com.whattoeat.domain.comment.dto;

import com.whattoeat.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        Long userId,
        String nickname,
        LocalDateTime createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getCreatedAt());
    }
}
