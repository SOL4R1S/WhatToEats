package com.whattoeat.external.kakao.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 로컬 API 장소 검색 응답의 개별 문서(document).
 * snake_case JSON 키를 camelCase 프로퍼티로 매핑한다.
 */
data class KakaoPlaceDocument(
    @JsonProperty("address_name")
    val addressName: String? = null,
    @JsonProperty("id")
    val id: String? = null,
    @JsonProperty("phone")
    val phone: String? = null,
    @JsonProperty("road_address_name")
    val roadAddressName: String? = null,
    /** 경도(longitude) */
    @JsonProperty("x")
    val x: Double? = null,
    /** 위도(latitude) */
    @JsonProperty("y")
    val y: Double? = null,
    @JsonProperty("category_name")
    val categoryName: String? = null,
    @JsonProperty("place_name")
    val placeName: String? = null,
    /** 요청 좌표로부터의 거리(미터). category 검색 시에만 제공. */
    @JsonProperty("distance")
    val distance: String? = null,
)
