package com.whattoeat.global.exception;

public class FeedNotFoundException extends RuntimeException {

    public FeedNotFoundException(Long id) {
        super("Feed not found: " + id);
    }
}
