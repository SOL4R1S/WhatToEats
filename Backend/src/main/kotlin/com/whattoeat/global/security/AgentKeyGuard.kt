package com.whattoeat.global.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * 헤르메스 에이전트용 API 키 가드.
 * 서버 env(app.agent-key)에만 존재하는 시크릿으로, 에이전트가 신고 검토 큐를
 * 주기적으로 폴링할 때 X-Agent-Key 헤더로 인증한다. ADMIN JWT와 별개 경로.
 */
@Component("agentKeyGuard")
class AgentKeyGuard(
    @Value("\${app.agent-key:}") private val agentKey: String,
) {
    fun matches(candidate: String?): Boolean =
        agentKey.isNotBlank() && candidate != null && constantTimeEquals(agentKey, candidate)

    /** 타이밍 공격 완화를 위한 상수시간 비교 */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }
}
