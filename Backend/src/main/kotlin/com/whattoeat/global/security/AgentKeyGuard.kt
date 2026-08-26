package com.whattoeat.global.security

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * 헤르메스 에이전트용 API 키 가드.
 * 서버 env(AGENT_KEY → APP_AGENT_KEY)에만 존재하는 시크릿으로, 에이전트가 신고 검토 큐를
 * 주기적으로 폴링할 때 X-Agent-Key 헤더로 인증한다. ADMIN JWT와 별개 경로.
 *
 * 주의: @Value("${app.agent-key}")는 relaxed binding이 없어서 APP_AGENT_KEY 환경변수를
 * 읽지 못한다(-key 포함 시 특히). 그래서 Environment에서 후보 키들을 명시적으로 조회한다.
 */
@Component("agentKeyGuard")
class AgentKeyGuard(private val environment: Environment) {

    fun matches(candidate: String?): Boolean {
        val expected = listOf("app.agent-key", "APP_AGENT_KEY", "AGENT_KEY")
            .firstNotNullOfOrNull { environment.getProperty(it) }
            ?.trim()
            .orEmpty()
        if (expected.isBlank() || candidate.isNullOrBlank()) return false
        return constantTimeEquals(expected, candidate)
    }

    /** 타이밍 공격 완화를 위한 상수시간 비교 */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }
}
