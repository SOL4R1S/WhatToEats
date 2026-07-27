package com.whattoeat.global.exception;

public class ListNotFoundException extends RuntimeException {
    public ListNotFoundException(Long id) {
        super("리스트를 찾을 수 없습니다: " + id);
    }
}
