package com.whattoeat.domain.report.dto

import com.whattoeat.domain.report.entity.Report
import com.whattoeat.domain.report.entity.ReportReason
import com.whattoeat.domain.report.entity.ReportStatus
import com.whattoeat.domain.report.entity.ReportTargetType
import jakarta.validation.constraints.Size

data class ReportCreateRequest(
    val reason: ReportReason,
    @field:Size(max = 500, message = "신고 내용은 500자 이하로 입력해주세요.")
    val detail: String? = null,
)

data class ReportResponse(
    val reportId: Long,
    val targetType: ReportTargetType,
    val feedId: Long?,
    val commentId: Long?,
    val restaurantListId: Long?,
    val reason: ReportReason,
    val detail: String?,
    val status: ReportStatus,
    val ruleScore: Int,
    val matchedRules: String?,
    val createdAt: String?,
) {
    companion object {
        fun from(report: Report): ReportResponse = ReportResponse(
            reportId = requireNotNull(report.id),
            targetType = report.targetType,
            feedId = report.feedId,
            commentId = report.commentId,
            restaurantListId = report.restaurantListId,
            reason = report.reason,
            detail = report.detail,
            status = report.status,
            ruleScore = report.ruleScore,
            matchedRules = report.matchedRules,
            createdAt = report.createdAt?.toString(),
        )
    }
}

/** 에이전트/어드민용 상세 뷰 (대상 콘텐츠 스냅샷 포함) */
data class AdminReportView(
    val reportId: Long,
    val targetType: ReportTargetType,
    val targetId: Long?,
    val targetContent: String?,   // 소프트삭제 후에도 판정 근거로 볼 수 있게 원문 포함
    val targetAuthorId: Long?,
    val reporterId: Long,
    val reportedUserId: Long?,
    val reason: ReportReason,
    val detail: String?,
    val status: ReportStatus,
    val ruleScore: Int,
    val matchedRules: String?,
    val resolutionNote: String?,
    val resolvedAt: String?,
    val createdAt: String?,
)

data class ResolutionRequest(
    @field:Size(max = 500, message = "처리 메모는 500자 이하로 입력해주세요.")
    val note: String? = null,
)
