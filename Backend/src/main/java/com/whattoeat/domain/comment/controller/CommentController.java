package com.whattoeat.domain.comment.controller;

import com.whattoeat.domain.comment.dto.CommentRequest;
import com.whattoeat.domain.comment.dto.CommentResponse;
import com.whattoeat.domain.comment.service.CommentService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feeds")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    //댓글 조회
    @GetMapping("/{feedId}/comments")
    public ResponseEntity<RsData<List<CommentResponse>>> getComments(@PathVariable Long feedId) {
        List<CommentResponse> comments = commentService.getComments(feedId);
        return ResponseEntity.ok(RsData.success(comments,"댓글 목록 조회 성공"));
    }

    //댓글 작성
    @PostMapping("/{feedId}/comments")
    public ResponseEntity<RsData<CommentResponse>> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long feedId,
            @RequestBody @Valid CommentRequest request) {
        CommentResponse comment = commentService.createComment(feedId,userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RsData.success(comment,"댓글 작성 성공"));
    }

    //댓글 삭제
    @DeleteMapping("/{feedId}/comments/{commentId}")
    public ResponseEntity<RsData<Void>> deleteComment(
            @PathVariable Long feedId,
            @PathVariable Long commentId) {
        commentService.deleteComment(feedId, commentId);
        return ResponseEntity.ok(RsData.success(null,"댓글이 삭제되었습니다."));
    }
}
