package com.whattoeat.global.exception;

public class SelfFollowNotAllowedException extends RuntimeException {

    public SelfFollowNotAllowedException() {
        super("자기 자신을 팔로우할 수 없습니다.");
    }
}
