package com.whattoeat.global.dummy

import com.whattoeat.domain.user.entity.Provider
import com.whattoeat.domain.user.entity.Role
import com.whattoeat.domain.user.entity.User
import com.whattoeat.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 운영(prod) 전용 어드민 초기 계정 시드.
 * 서버 env에 ADMIN_USERNAME / ADMIN_PASSWORD가 있으면 해당 계정을 ADMIN 권한으로 생성한다.
 * 이미 존재하면 비밀번호를 갱신한다(비밀번호 회전 지원).
 */
@Component
@Order(100)
class AdminBootstrap(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${app.admin.username:}")
    private var adminUsername: String = ""

    @Value("\${app.admin.password:}")
    private var adminPassword: String = ""

    @Transactional
    override fun run(vararg args: String) {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            log.info("[Admin] app.admin.username/password 미설정 → 어드민 계정 생성 생략")
            return
        }

        val existing = userRepository.findByLoginId(adminUsername)
        if (existing.isPresent) {
            log.info("[Admin] {} 계정 이미 존재 — 기존 계정을 관리자 로그인에 사용합니다", adminUsername)
        } else {
            val user = User.builder()
                .loginId(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .nickname("관리자")
                .email("")
                .provider(Provider.LOCAL)
                .role(Role.ADMIN)
                .build()
            userRepository.save(user)
            log.info("[Admin] 관리자 계정 생성: {}", adminUsername)
        }
    }
}