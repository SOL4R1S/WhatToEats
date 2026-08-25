package com.whattoeat.external.kakao.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 로컬 API 장소 검색 응답 전체.
 */
data class KakaoPlaceResponse(
    @JsonProperty("documents")
    val documents: List<KakaoPlaceDocument> = emptyList(),
    @JsonProperty("meta")
    val meta: Meta = Meta(),
) {
    data class Meta(
        @JsonProperty("total_count")
        val totalCount: Int = 0,
        @JsonProperty("pageable_count")
        val pageableCount: Int = 0,
        @JsonProperty("is_end")
        val isEnd: Boolean = false,
    )
}
