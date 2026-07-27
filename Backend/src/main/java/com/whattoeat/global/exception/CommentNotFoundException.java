package com.whattoeat.global.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException(Long id) {
        super("Comment not found: " + id);
    }
}
