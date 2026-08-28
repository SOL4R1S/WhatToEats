package com.whattoeat.domain.recommendation.dto

import com.whattoeat.domain.restaurant.dto.RestaurantRequest
import com.whattoeat.domain.restaurant.entity.Category
import com.whattoeat.domain.restaurant.entity.MoodTag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class RecommendRequest(
    @field:NotEmpty(message = "추천 후보 목록은 비어있을 수 없습니다.")
    val candidates: List<@Valid RestaurantRequest.FromKakao>,
    val category: Category? = null,
    val sort: RecommendSort = RecommendSort.RANDOM,
    // 선택 지역 중심 좌표 (추천 탭에서 유저가 고른 동/구의 좌표)
    val lat: Double? = null,
    val lng: Double? = null,
    val exclude: List<String> = emptyList(),
    val mood: MoodTag? = null,
    // 선택 지역 중심(lat/lng)으로부터 후보를 허용할 최대 거리(미터). null이면 반경 필터링 안 함.
    val maxDistanceMeter: Int? = null,
)
