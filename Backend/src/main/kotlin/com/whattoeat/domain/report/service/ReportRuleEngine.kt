package com.whattoeat.domain.report.service

import com.whattoeat.domain.report.entity.ReportReason
import org.springframework.stereotype.Component

/**
 * 룰 기반 증불 스코어 계산기.
 *
 * ⚠️ 이 점수는 "자동 삭제"에 사용되지 않는다. 전부 PENDING 대기열로 가며,
 *    헤르메스 에이전트가 주기적 검토 때 우선순위와 판단 근거로 참고하는 메타데이터다.
 */
@Component
class ReportRuleEngine {

    data class RuleResult(val score: Int, val matchedRules: List<String>)

    fun evaluate(
        reason: ReportReason,
        content: String?,
        reporterReportCount: Long, // 신고자의 최근 누적 신고 수 (저품질 신고자 판별용)
        targetReportCount: Long,   // 같은 대상에 대한 누적 신고 수
    ): RuleResult {
        var score = 0
        val matched = mutableListOf<String>()

        // 사유별 기본 가중치
        val reasonWeight = when (reason) {
            ReportReason.PORNOGRAPHY -> 40
            ReportReason.FRAUD -> 35
            ReportReason.ABUSE -> 30
            ReportReason.SPAM -> 25
            ReportReason.COPYRIGHT -> 15
            ReportReason.OTHER -> 5
        }
        score += reasonWeight
        matched += "reason:${reason.name}($reasonWeight)"

        // 콘텐츠 키워드 룰 (간단한 1차 필터 — 에이전트 검토 시 힌트)
        val text = content.orEmpty().lowercase()
        SPAM_KEYWORDS.forEach { kw ->
            if (kw in text) {
                score += 20
                matched += "keyword:$kw"
            }
        }

        // 동일 대상 누적 신고 수가 많을수록 신빙성 상승
        if (targetReportCount >= 3) {
            score += 15
            matched += "multi_reporters($targetReportCount)"
        } else if (targetReportCount >= 2) {
            score += 8
            matched += "multi_reporters($targetReportCount)"
        }

        // 남발 신고자 감점 (신고를 많이 한 유저일수록 개별 신고 신뢰도 하락)
        if (reporterReportCount >= 50) {
            score -= 20
            matched += "reporter_flood(-20)"
        } else if (reporterReportCount >= 10) {
            score -= 5
            matched += "reporter_active(-5)"
        }

        return RuleResult(score.coerceAtLeast(0), matched)
    }

    companion object {
        private val SPAM_KEYWORDS = listOf(
            "http://", "https://", "www.", ".com ", ".net ", "카지노", "토토",
            "먹튀", "대출", "리크루팅", "초대코드", "프로모션코드", "telegram",
            "whatsapp", "bit.ly", "카톡+", "문의주세요", "광고입니다",
        )
    }
}
