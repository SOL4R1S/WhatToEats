package com.whattoeat.domain.auth.dto

import jakarta.validation.constraints.NotBlank

@JvmRecord
data class LoginRequest(
    // 로그인 아이디는 일반(이메일) 계정과 비이메일 관리자 계정(AdminBootstrap 시드)을
    // 모두 수용해야 하므로 이메일 형식 검증을 걸지 않는다.
    // 이메일 형식 강제는 가입(SignUpRequest) 단계에서만 적용된다.
    @field:NotBlank val loginId: String,
    @field:NotBlank val password: String
)
