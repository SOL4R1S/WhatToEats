package com.whattoeat.global.exception;

public class FeedLikeNotFoundException extends RuntimeException {

    public FeedLikeNotFoundException() {
        super("좋아요 관계가 존재하지 않습니다.");
    }
}
