package com.whattoeat.domain.restaurant.service

import com.whattoeat.domain.restaurant.entity.Restaurant
import com.whattoeat.domain.restaurant.repository.RestaurantRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class RestaurantServiceTest {

    @Mock
    lateinit var restaurantRepository: RestaurantRepository

    @InjectMocks
    lateinit var restaurantService: RestaurantService

    private fun restaurant(
        id: Long,
        name: String,
        address: String
    ): Restaurant {
        val r = Restaurant(
            kakaoPlaceId = "kakao-$id",
            name = name,
            category = com.whattoeat.domain.restaurant.entity.Category.KOREAN,
            address = address,
            roadAddress = null,
            region1 = "부산",
            region2 = "해운대구",
            region3 = null,
            region4 = null,
            phone = null,
            lat = 35.1,
            lng = 129.1
        )
        org.springframework.test.util.ReflectionTestUtils.setField(r, "id", id)
        return r
    }

    @Test
    fun `이름 LIKE 검색 - 부분 일치 반환`() {
        given(restaurantRepository.findByNameContainingIgnoreCase("해운대"))
            .willReturn(listOf(restaurant(1L, "해운대 맛집", "부산 해운대구"), restaurant(2L, "해운대 회센터", "부산 해운대구")))

        val result = restaurantService.searchByName("해운대")

        assertThat(result).hasSize(2)
        assertThat(result.map { it.name }).containsExactly("해운대 맛집", "해운대 회센터")
    }

    @Test
    fun `주소 LIKE 검색 - 부분 일치 반환`() {
        given(restaurantRepository.findByAddressContainingIgnoreCase("수영구"))
            .willReturn(listOf(restaurant(1L, "광안리 맛집", "부산 수영구 광안동")))

        val result = restaurantService.searchByAddress("수영구")

        assertThat(result).hasSize(1)
        assertThat(result[0].address).contains("수영구")
    }

    @Test
    fun `이름 LIKE 검색 - 결과 없으면 빈 목록`() {
        given(restaurantRepository.findByNameContainingIgnoreCase("없는식당"))
            .willReturn(emptyList())

        val result = restaurantService.searchByName("없는식당")

        assertThat(result).isEmpty()
    }
}
