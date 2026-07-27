package com.whattoeat.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoPlaceResponse {
    @JsonProperty("documents")
    private List<KakaoPlaceDocument> documents;

    @JsonProperty("meta")
    private Meta meta;

    @Getter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("total_count")
        private int totalCount;
        @JsonProperty("pageable_count")
        private int pageableCount;
        @JsonProperty("is_end")
        private boolean isEnd;
    }
}
