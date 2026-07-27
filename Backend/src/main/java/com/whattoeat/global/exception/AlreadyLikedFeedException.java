package com.whattoeat.global.exception;

public class AlreadyLikedFeedException extends RuntimeException {

    public AlreadyLikedFeedException() {
        super("이미 좋아요한 피드입니다.");
    }
}
