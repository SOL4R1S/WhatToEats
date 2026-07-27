package com.whattoeat.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoPlaceDocument {
    @JsonProperty("address_name")
    private String addressName;
    @JsonProperty("id")
    private String id;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("road_address_name")
    private String roadAddressName;
    @JsonProperty("x")
    private Double x;
    @JsonProperty("y")
    private Double y;
    @JsonProperty("category_name")
    private String categoryName;
    @JsonProperty("place_name")
    private String placeName;
    @JsonProperty("distance")
    private String distance;
}
