package com.whattoeat.domain.restaurant.entity;

import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "restaurant",
        indexes = {
                @Index(name = "idx_restaurant_category", columnList = "category"),
                @Index(name = "idx_restaurant_region1", columnList = "region1"),
                @Index(name = "idx_restaurant_region2", columnList = "region2"),
                @Index(name = "idx_restaurant_region3", columnList = "region3"),
                @Index(name = "idx_restaurant_region4", columnList = "region4")
        }
)
@Getter
@NoArgsConstructor
public class Restaurant extends BaseEntity {
    @Column(name = "kakao_place_id", nullable = false, unique = true, length = 255)
    private String kakaoPlaceId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 100)
    private Category category;

    @Column(name = "address", nullable = false, length = 300)
    private String address;

    @Column(name = "road_address", length = 300)
    private String roadAddress;

    @Column(name = "region1", nullable = false, length = 50)
    private String region1;

    @Column(name = "region2", nullable = false, length = 50)
    private String region2;

    @Column(name = "region3", length = 50)
    private String region3;

    @Column(name = "region4", length = 50)
    private String region4;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lng", nullable = false)
    private double lng;

    public Restaurant(
            String kakaoPlaceId,
            String name,
            Category category,
            String address,
            String roadAddress,
            String region1,
            String region2,
            String region3,
            String region4,
            String phone,
            double lat,
            double lng
    ) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.roadAddress = roadAddress;
        this.region1 = region1;
        this.region2 = region2;
        this.region3 = region3;
        this.region4 = region4;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
    }

}
