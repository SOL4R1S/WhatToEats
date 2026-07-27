package com.whattoeat.global.rsData;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RsData<T>(
        boolean success,
        T data,
        String message
) {
    // 성공 응답 - message 있음
    public static <T> RsData<T> success(T data, String message) {
        return new RsData<>(
                true,
                data,
                message
        );
    }

    // 성공 응답 - message 없음
    public static <T> RsData<T> success(T data) {
        return new RsData<>(
                true,
                data,
                null
        );
    }

    public static <T> RsData<T> failure(String message) {
        return new RsData<>(false, null, message);
    }

    public static <T> RsData<T> failure(T data, String message) {
        return new RsData<>(false, data, message);
    }
}
