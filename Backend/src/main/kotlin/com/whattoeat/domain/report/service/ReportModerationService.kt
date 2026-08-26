package com.whattoeat.domain.report.service

import com.whattoeat.domain.comment.repository.CommentRepository
import com.whattoeat.domain.feed.repository.FeedRepository
import com.whattoeat.domain.report.dto.AdminReportView
import com.whattoeat.domain.report.entity.Report
import com.whattoeat.domain.report.entity.ReportStatus
import com.whattoeat.domain.report.entity.ReportTargetType
import com.whattoeat.domain.report.repository.ReportRepository
import com.whattoeat.domain.restaurantlist.repository.RestaurantListRepository
import com.whattoeat.global.exception.ReportNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 신고 검열 파이프라인.
 * 헤르메스 에이전트(또는 ADMIN 권한 사용자)가 PENDING 대기열을 주기적으로 검토하고,
 * 판정 결과에 따라 대상 콘텐츠를 소프트삭제/복원한다.
 */
@Service
class ReportModerationService(
    private val reportRepository: ReportRepository,
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val restaurantListRepository: RestaurantListRepository,
) {

    @Transactional(readOnly = true)
    fun pendingQueue(limit: Int): List<AdminReportView> =
        reportRepository.findByStatusOrderByIdAsc(
            ReportStatus.PENDING,
            PageRequest.of(0, limit.coerceIn(1, 100)),
        ).map(::toView)

    /** 처리 이력 (RESOLVED/REJECTED/RESTORED 최근 순 — 어드민 삭제내역 화면) */
    @Transactional(readOnly = true)
    fun history(limit: Int): List<AdminReportView> =
        reportRepository.findByStatusInOrderByResolvedAtDesc(
            setOf(ReportStatus.RESOLVED, ReportStatus.REJECTED, ReportStatus.RESTORED),
            PageRequest.of(0, limit.coerceIn(1, 100)),
        ).map(::toView)

    @Transactional(readOnly = true)
    fun counts(): Map<String, Long> = mapOf(
        "pending" to reportRepository.countByStatus(ReportStatus.PENDING),
        "resolved" to reportRepository.countByStatus(ReportStatus.RESOLVED),
        "rejected" to reportRepository.countByStatus(ReportStatus.REJECTED),
        "restored" to reportRepository.countByStatus(ReportStatus.RESTORED),
    )

    /** 판정: 삭제 확정 — 대상 콘텐츠 소프트삭제 */
    @Transactional
    fun resolve(reportId: Long, note: String?): AdminReportView {
        val report = getReport(reportId)
        softDeleteTarget(report)
        report.resolve(note ?: "에이전트 검토 후 삭제 확정")
        return toView(report)
    }

    /** 판정: 기각 — 콘텐츠 유지 */
    @Transactional
    fun reject(reportId: Long, note: String?): AdminReportView {
        val report = getReport(reportId)
        report.reject(note ?: "에이전트 검토 후 기각")
        return toView(report)
    }

    /** 롤백 — 소프트삭제 복원 */
    @Transactional
    fun restore(reportId: Long, note: String?): AdminReportView {
        val report = getReport(reportId)
        restoreTarget(report)
        report.restore(note ?: "관리자 롤백")
        return toView(report)
    }

    // ---- 내부 ----

    private fun getReport(reportId: Long): Report =
        reportRepository.findById(reportId).orElseThrow { ReportNotFoundException(reportId) }

    private fun softDeleteTarget(report: Report) {
        when (report.targetType) {
            ReportTargetType.FEED -> report.feedId?.let { id ->
                feedRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.softDelete()
            }
            ReportTargetType.COMMENT -> report.commentId?.let { id ->
                commentRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.softDelete()
            }
            ReportTargetType.RESTAURANT_LIST -> report.restaurantListId?.let { id ->
                restaurantListRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.softDelete()
            }
        }
    }

    private fun restoreTarget(report: Report) {
        when (report.targetType) {
            ReportTargetType.FEED -> report.feedId?.let { id ->
                feedRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.restore()
            }
            ReportTargetType.COMMENT -> report.commentId?.let { id ->
                commentRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.restore()
            }
            ReportTargetType.RESTAURANT_LIST -> report.restaurantListId?.let { id ->
                restaurantListRepository.findById(id).orElseThrow { ReportNotFoundException(id) }.restore()
            }
        }
    }

    private fun toView(report: Report): AdminReportView {
        var targetContent: String? = null
        var targetAuthorId: Long? = null

        runCatching {
            when (report.targetType) {
                ReportTargetType.FEED -> report.feedId?.let { id ->
                    feedRepository.findById(id).orElse(null)?.let {
                        targetContent = it.content
                        targetAuthorId = it.user.id
                    }
                }
                ReportTargetType.COMMENT -> report.commentId?.let { id ->
                    commentRepository.findById(id).orElse(null)?.let {
                        targetContent = it.content
                        targetAuthorId = it.user.id
                    }
                }
                ReportTargetType.RESTAURANT_LIST -> report.restaurantListId?.let { id ->
                    restaurantListRepository.findById(id).orElse(null)?.let {
                        targetContent = "${it.title} - ${it.description}"
                        targetAuthorId = it.user.id
                    }
                }
            }
        } // 대상이 물리삭제된 경우에도 신고 자체의 판정은 가능해야 하므로 조용히 무시

        return AdminReportView(
            reportId = requireNotNull(report.id),
            targetType = report.targetType,
            targetId = report.feedId ?: report.commentId ?: report.restaurantListId,
            targetContent = targetContent,
            targetAuthorId = targetAuthorId,
            reporterId = report.reporterId,
            reportedUserId = report.reportedUserId,
            reason = report.reason,
            detail = report.detail,
            status = report.status,
            ruleScore = report.ruleScore,
            matchedRules = report.matchedRules,
            resolutionNote = report.resolutionNote,
            resolvedAt = report.resolvedAt?.toString(),
            createdAt = report.createdAt?.toString(),
        )
    }
}
