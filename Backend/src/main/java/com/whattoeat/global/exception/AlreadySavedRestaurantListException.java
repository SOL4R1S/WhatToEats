package com.whattoeat.global.exception;

public class AlreadySavedRestaurantListException extends RuntimeException {
    public AlreadySavedRestaurantListException() {
        super("이미 저장한 리스트입니다.");
    }
}
