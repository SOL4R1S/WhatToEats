package com.whattoeat.global.exception;

public class DuplicateRestaurantListItemException extends RuntimeException {
    public DuplicateRestaurantListItemException(Long id) {
        super("이미 리스트에 추가된 식당입니다. : " + id);
    }
}
