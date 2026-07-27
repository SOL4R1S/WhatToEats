package com.whattoeat.global.exception;

public class RestaurantListItemNotFoundException extends RuntimeException {
    public RestaurantListItemNotFoundException(Long id) {
        super("식당 리스트 아이템을 찾을 수 없습니다: " + id);
    }
}
