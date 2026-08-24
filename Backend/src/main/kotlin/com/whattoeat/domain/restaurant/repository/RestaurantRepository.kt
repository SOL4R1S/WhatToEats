package com.whattoeat.domain.restaurant.repository

import com.whattoeat.domain.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface RestaurantRepository : JpaRepository<Restaurant, Long> {
     fun findByKakaoPlaceId(kakaoPlaceId: String) : Optional<Restaurant>
    fun findByKakaoPlaceIdIn(kakaoPlaceIds: List<String>) : List<Restaurant>
    fun findByNameContainingIgnoreCase(name: String) : List<Restaurant>
    fun findByAddressContainingIgnoreCase(address: String) : List<Restaurant>

    /**
     * 한글 부분일치 검색: FULLTEXT ngram 인덱스 기반 BOOLEAN MODE.
     * - LIKE '%키워드%'는 인덱스를 못 타지만, FULLTEXT는 %키워드% 부분일치도 인덱스로 처리.
     * - BOOLEAN MODE를 써야 한글 3글자 검색어("해운대")가 2글자 토큰("해운")으로 분해되어 매칭됨.
     * - 검색어의 공백/특수문자는 ngram 파서가 자연 처리하므로 그대로 전달.
     */
    @Query(
        value = "SELECT * FROM restaurant WHERE MATCH(name) AGAINST(:keyword IN BOOLEAN MODE) ORDER BY created_at DESC",
        nativeQuery = true
    )
    fun searchByNameFullText(@Param("keyword") keyword: String): List<Restaurant>
}
