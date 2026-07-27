package com.whattoeat.global.exception;

public class DuplicateNicknameException extends RuntimeException{
    public DuplicateNicknameException(String message) {
        super(message);
    }
}
