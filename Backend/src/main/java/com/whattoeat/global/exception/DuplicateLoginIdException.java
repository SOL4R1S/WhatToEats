package com.whattoeat.global.exception;

public class DuplicateLoginIdException extends RuntimeException{
    public DuplicateLoginIdException(String message) {
        super(message);
    }
}
