package com.whattoeat.global.exception;

public class AlreadyFollowingException extends RuntimeException {

    public AlreadyFollowingException() {
        super("이미 팔로우 중인 사용자입니다.");
    }
}
