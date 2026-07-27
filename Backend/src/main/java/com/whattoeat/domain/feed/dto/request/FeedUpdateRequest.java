package com.whattoeat.domain.feed.dto.request;

import jakarta.validation.constraints.*;

public record FeedUpdateRequest(
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max=1000, message = "내용은 1000자를 넘을 수 없습니다.")
        String content,

        @Positive(message = "음수 Id는 올 수 없습니다.")
        Long restaurantId,

        boolean deleteImage

) {
}
