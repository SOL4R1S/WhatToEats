package com.whattoeat.domain.restaurantlist.repository;

import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RestaurantListRepository extends JpaRepository<RestaurantList, Long> {
    // 내 리스트 조회 : 최신 생성 리스트가 위로 오게 id 내림차순
    Page<RestaurantList> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Optional<RestaurantList> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select distinct rl
          from RestaurantList rl
          left join fetch rl.items i
          left join fetch i.restaurant
         where rl.id = :id
    """)
    Optional<RestaurantList> findByIdWithItems(@Param("id") Long id);


    Page<RestaurantList> findByUserIdNot(Long userId, Pageable pageable);
}
