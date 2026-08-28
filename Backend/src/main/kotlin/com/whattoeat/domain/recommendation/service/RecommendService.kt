package com.whattoeat.domain.recommendation.service

import com.whattoeat.domain.feed.repository.FeedRepository
import com.whattoeat.domain.recommendation.dto.RecommendItem
import com.whattoeat.domain.recommendation.dto.RecommendRequest
import com.whattoeat.domain.recommendation.dto.RecommendSort
import com.whattoeat.domain.restaurant.dto.RestaurantRequest
import com.whattoeat.domain.restaurant.entity.Category
import com.whattoeat.domain.restaurant.entity.MoodTag
import com.whattoeat.domain.restaurant.mapper.categoryLabel
import com.whattoeat.domain.restaurant.mapper.toCategory
import com.whattoeat.domain.restaurant.repository.RestaurantRepository
import com.whattoeat.domain.restaurantlist.repository.RestaurantListItemRepository
import com.whattoeat.global.exception.InvalidRecommendParameterException
import com.whattoeat.global.exception.RestaurantNotFoundException
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import org.springframework.stereotype.Service

@Service
class RecommendService(
    private val restaurantRepository: RestaurantRepository,
    private val feedRepository: FeedRepository,
    private val restaurantListItemRepository: RestaurantListItemRepository
) {

    fun recommend(request: RecommendRequest): List<RecommendItem> {
        validateCoordinates(request.lat, request.lng)

        val seen = mutableSetOf<String>()
        val items = request.candidates
            .filter { it.kakaoPlaceId.isNotBlank() }                    // ID 없는 후보 제외
            .filter { seen.add(it.kakaoPlaceId) }                       // 카카오 ID 중복 제거
            .filter { isFoodOrCafe(it.categoryName) }                   // 만화방/놀이시설 등 제외
            .filter { matchesCategory(it.categoryName, request.category) } // 선택 카테고리 일치만
            .filter { it.kakaoPlaceId !in request.exclude }             // 이미 본 식당 제외
            .filter { isWithinRegionRadius(it, request) }               // ★ 선택 지역 좌표 기준 반경 검증
            .map { toItem(it) }

        if (items.isEmpty()) {
            throw RestaurantNotFoundException("조건에 맞는 식당이 없습니다.")
        }

        return when (request.sort) {
            RecommendSort.RANDOM -> filterByMoodScore(items, request.mood)
            // 거리순도 mood 필터를 먼저 적용한 뒤 카카오가 계산한 distanceMeter로 정렬
            // (mood+거리순 조합 일관성)
            RecommendSort.DISTANCE -> filterByMoodScore(items, request.mood)
                .sortedBy { it.distanceMeter ?: Int.MAX_VALUE }
        }
    }

    private fun filterByMoodScore(items: List<RecommendItem>, mood: MoodTag?): List<RecommendItem> {
        if (mood == null) return items.shuffled()

        val kakaoToDbId = restaurantRepository
            .findByKakaoPlaceIdIn(items.map { it.kakaoPlaceId })
            .mapNotNull { restaurant -> restaurant.id?.let { id -> restaurant.kakaoPlaceId to id } }.toMap()
        val dbIds = kakaoToDbId.values.toList()

        val votes = mutableMapOf<Long, Long>()

        if (dbIds.isNotEmpty()) {
            feedRepository.countMoodVotes(mood, dbIds)
                .forEach { votes.merge(it.restaurantId, it.voteCount, Long::plus) }
            restaurantListItemRepository.countMoodVotes(mood, dbIds)
                .forEach { votes.merge(it.restaurantId, it.voteCount, Long::plus) }
        }

        val matched = items.filter { item ->
            val ruleScore = moodRuleScore(mood, item.category, item.name, item.categoryName)
            val voteScore = votes[kakaoToDbId[item.kakaoPlaceId]] ?: 0L
            ruleScore + voteScore > 0
        }
        return (matched.ifEmpty { items }).shuffled()
    }

    // 1단계 분류가 음식점/카페인 것만 (여가시설 > 보드카페 같은 비카페 CE7 노이즈 제거)
    private fun isFoodOrCafe(categoryName: String?): Boolean {
        val top = categoryName.orEmpty().split(">").firstOrNull()?.trim()
        return top == "음식점" || top == "카페"
    }

    private fun matchesCategory(categoryName: String?, category: Category?): Boolean =
        category == null || category == Category.ETC || toCategory(categoryName) == category

    // 선택 지역(lat/lng) 기준 maxDistanceMeter(미터) 반경 밖 후보 제외.
    // 카카오 검색이 지역 중심 좌표 기준으로 이뤄지더라도, GPS 기반 등으로 섞여 들어온
    // 먼 곳 후보를 서버에서 한 번 더 걸러내는 안전망. lat/lng 또는 maxDistanceMeter가
    // 없으면(시/도 전체 선택 등) 필터링하지 않는다.
    private fun isWithinRegionRadius(
        candidate: RestaurantRequest.FromKakao,
        request: RecommendRequest,
    ): Boolean {
        val maxMeters = request.maxDistanceMeter ?: return true
        val lat = request.lat ?: return true
        val lng = request.lng ?: return true
        return haversineMeters(candidate.lat, candidate.lng, lat, lng) <= maxMeters
    }

    /** 두 좌표(위도/경도, 도 단위) 사이의 직선 거리(미터). Haversine 공식. */
    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2)
        return earthRadiusMeters * 2 * asin(sqrt(a))
    }

    private fun toItem(candidate: RestaurantRequest.FromKakao): RecommendItem =
        RecommendItem(
            kakaoPlaceId = candidate.kakaoPlaceId,
            name = candidate.name,
            categoryName = candidate.categoryName,
            category = toCategory(candidate.categoryName),
            categoryLabel = categoryLabel(candidate.categoryName),
            // 카카오맵 JS SDK가 sort=distance로 계산해준 거리(미터)를 그대로 사용.
            // 서버에서 직접 계산하지 않는다 (카카오 API 우선 활용 원칙).
            distanceMeter = candidate.distanceMeter,
        )

    private fun validateCoordinates(lat: Double?, lng: Double?) {
        if (lat != null && lat !in -90.0..90.0) {
            throw InvalidRecommendParameterException("lat는 -90~90 사이여야 합니다.")
        }
        if (lng != null && lng !in -180.0..180.0) {
            throw InvalidRecommendParameterException("lng는 -180~180 사이여야 합니다.")
        }
        if ((lat == null) != (lng == null)) {
            throw InvalidRecommendParameterException("lat와 lng는 함께 전달해야 합니다.")
        }
    }
}