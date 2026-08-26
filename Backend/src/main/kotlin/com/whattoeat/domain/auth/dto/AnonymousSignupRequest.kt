package com.whattoeat.domain.auth.dto

import jakarta.validation.constraints.Size

/**
 * 그린메일(익명 가입) 요청.
 * deviceKey는 클라이언트가 생성해 로컬 키체인에 보관하는 랜덤 값(16자 이상)으로,
 * 서버는 이것을 해시해서 login_id로 사용한다. 이메일·비밀번호는 수집하지 않는다.
 */
data class AnonymousSignupRequest(
    @field:Size(min = 16, max = 128, message = "기기 키는 16~128자여야 합니다.")
    val deviceKey: String,
)
