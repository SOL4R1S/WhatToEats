package com.whattoeat.global.exception;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(Long id) {
        super("식당을 찾을 수 없습니다: " + id);
    }

    public RestaurantNotFoundException(String message) {
        super(message);
    }
}
