package com.whattoeat.domain.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** 익명 가입 응답: 앱 등 쿠키 미지원 클라이언트를 위해 토큰을 JSON 바디로도 전달 */
data class AnonymousAuthResponse(
    @JsonProperty("accessToken")
    val accessToken: String,
    @JsonProperty("refreshToken")
    val refreshToken: String,
    @JsonProperty("userProfile")
    val userProfile: AuthUserResponse,
)
