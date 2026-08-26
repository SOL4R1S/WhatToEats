package com.whattoeat.domain.report.entity

import com.whattoeat.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

/** 신고 대상 종류 */
enum class ReportTargetType { FEED, COMMENT, RESTAURANT_LIST }

/** 신고 사유 */
enum class ReportReason { SPAM, ABUSE, PORNOGRAPHY, FRAUD, COPYRIGHT, OTHER }

/**
 * 신고 처리 상태.
 * - PENDING : 대기열 적재 (헤르메스 에이전트의 주기적 검토 대상)
 * - RESOLVED: 에이전트가 삭제 확정 (대상은 소프트삭제됨)
 * - REJECTED: 에이전트가 기각 (콘텐츠 유지)
 * - RESTORED: RESOLVED였던 것이 롤백으로 복원됨
 */
enum class ReportStatus { PENDING, RESOLVED, REJECTED, RESTORED }

@Entity
@Table(
    name = "report",
    indexes = [
        Index(name = "idx_report_status", columnList = "status"),
        Index(name = "idx_report_reporter", columnList = "reporter_id"),
    ],
)
class Report(
    targetType: ReportTargetType,
    reporterId: Long,
    reason: ReportReason,
) : BaseEntity() {

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    var targetType: ReportTargetType = targetType
        protected set

    // 정규화 관계 대신 ID만 보유: 대상이 소프트삭제되어도 신고 이력이 남아야 하므로.
    @Column(name = "feed_id")
    var feedId: Long? = null
        protected set

    @Column(name = "comment_id")
    var commentId: Long? = null
        protected set

    @Column(name = "restaurant_list_id")
    var restaurantListId: Long? = null
        protected set

    @Column(name = "reporter_id", nullable = false)
    var reporterId: Long = reporterId
        protected set

    @Column(name = "reported_user_id")
    var reportedUserId: Long? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var reason: ReportReason = reason
        protected set

    @Column(length = 500)
    var detail: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReportStatus = ReportStatus.PENDING
        protected set

    /** 룰 기반 증불 점수. 자동 삭제에는 사용하지 않고 에이전트 검토 우선순위 참고용. */
    @Column(name = "rule_score", nullable = false)
    var ruleScore: Int = 0
        protected set

    /** 걸린 룰 이름 목록 (콤마 구분). 에이전트 판단 근거. */
    @Column(name = "matched_rules", length = 500)
    var matchedRules: String? = null
        protected set

    /** 에이전트 판정 사유 메모 */
    @Column(name = "resolution_note", length = 500)
    var resolutionNote: String? = null
        protected set

    @Column(name = "resolved_at")
    var resolvedAt: LocalDateTime? = null
        protected set

    companion object {
        fun of(
            targetType: ReportTargetType,
            targetIds: Triple<Long?, Long?, Long?>, // (feedId, commentId, restaurantListId)
            reporterId: Long,
            reportedUserId: Long?,
            reason: ReportReason,
            detail: String?,
        ): Report {
            val report = Report(targetType, reporterId, reason)
            report.feedId = targetIds.first
            report.commentId = targetIds.second
            report.restaurantListId = targetIds.third
            report.reportedUserId = reportedUserId
            report.detail = detail?.take(500)
            return report
        }
    }

    /** 에이전트(또는 룰 갱신 배치)가 점수를 기록 — PENDING 상태에서만 갱신 가능 */
    fun applyRuleScore(score: Int, matchedRules: List<String>) {
        check(status == ReportStatus.PENDING) { "PENDING 상태에서만 룰 점수를 갱신할 수 있습니다." }
        this.ruleScore = score
        this.matchedRules = matchedRules.joinToString(",").take(500).ifEmpty { null }
    }

    /** 에이전트 판정: 삭제 확정 */
    fun resolve(note: String?) {
        require(status == ReportStatus.PENDING || status == ReportStatus.RESTORED) { "검토 가능한 상태가 아닙니다." }
        this.status = ReportStatus.RESOLVED
        this.resolutionNote = note?.take(500)
        this.resolvedAt = LocalDateTime.now()
    }

    /** 에이전트 판정: 기각 */
    fun reject(note: String?) {
        require(status == ReportStatus.PENDING || status == ReportStatus.RESOLVED) { "기각 가능한 상태가 아닙니다." }
        this.status = ReportStatus.REJECTED
        this.resolutionNote = note?.take(500)
        this.resolvedAt = LocalDateTime.now()
    }

    /** 롤백: 삭제했던 것 복원 */
    fun restore(note: String?) {
        require(status == ReportStatus.RESOLVED) { "RESOLVED 상태만 롤백할 수 있습니다." }
        this.status = ReportStatus.RESTORED
        this.resolutionNote = note?.take(500)
        this.resolvedAt = LocalDateTime.now()
    }
}