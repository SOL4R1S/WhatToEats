package com.whattoeat.domain.report.service

import com.whattoeat.domain.comment.repository.CommentRepository
import com.whattoeat.domain.feed.repository.FeedRepository
import com.whattoeat.domain.report.dto.ReportCreateRequest
import com.whattoeat.domain.report.dto.ReportResponse
import com.whattoeat.domain.report.entity.Report
import com.whattoeat.domain.report.entity.ReportReason
import com.whattoeat.domain.report.entity.ReportTargetType
import com.whattoeat.domain.report.repository.ReportRepository
import com.whattoeat.domain.restaurantlist.repository.RestaurantListRepository
import com.whattoeat.global.exception.ReportNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val feedRepository: FeedRepository,
    private val commentRepository: CommentRepository,
    private val restaurantListRepository: RestaurantListRepository,
    private val ruleEngine: ReportRuleEngine,
) {

    /** 신고 접수: 전부 PENDING 대기열로 적재 (자동 삭제 없음) */
    @Transactional
    fun reportFeed(reporterId: Long, feedId: Long, request: ReportCreateRequest): ReportResponse {
        if (reportRepository.existsByReporterIdAndTargetTypeAndFeedId(reporterId, ReportTargetType.FEED, feedId)) {
            throw IllegalStateException("이미 신고한 콘텐츠입니다.")
        }
        val feed = feedRepository.findById(feedId).orElseThrow { ReportNotFoundException(feedId) }

        val rule = ruleEngine.evaluate(
            reason = request.reason,
            content = feed.content,
            reporterReportCount = reportRepository.countByReporterId(reporterId),
            targetReportCount = reportRepository.countByFeedIdAndStatusIn(feedId, PENDING_STATUSES),
        )
        val report = Report.of(
            targetType = ReportTargetType.FEED,
            targetIds = Triple(feed.id, null, null),
            reporterId = reporterId,
            reportedUserId = feed.user.id,
            reason = request.reason,
            detail = request.detail,
        )
        report.applyRuleScore(rule.score, rule.matchedRules)
        return ReportResponse.from(reportRepository.save(report))
    }

    @Transactional
    fun reportComment(reporterId: Long, commentId: Long, request: ReportCreateRequest): ReportResponse {
        if (reportRepository.existsByReporterIdAndTargetTypeAndCommentId(reporterId, ReportTargetType.COMMENT, commentId)) {
            throw IllegalStateException("이미 신고한 콘텐츠입니다.")
        }
        val comment = commentRepository.findById(commentId).orElseThrow { ReportNotFoundException(commentId) }

        val rule = ruleEngine.evaluate(
            reason = request.reason,
            content = comment.content,
            reporterReportCount = reportRepository.countByReporterId(reporterId),
            targetReportCount = reportRepository.countByCommentIdAndStatusIn(commentId, PENDING_STATUSES),
        )
        val report = Report.of(
            targetType = ReportTargetType.COMMENT,
            targetIds = Triple(null, comment.id, null),
            reporterId = reporterId,
            reportedUserId = comment.user.id,
            reason = request.reason,
            detail = request.detail,
        )
        report.applyRuleScore(rule.score, rule.matchedRules)
        return ReportResponse.from(reportRepository.save(report))
    }

    @Transactional
    fun reportRestaurantList(reporterId: Long, listId: Long, request: ReportCreateRequest): ReportResponse {
        if (reportRepository.existsByReporterIdAndTargetTypeAndRestaurantListId(reporterId, ReportTargetType.RESTAURANT_LIST, listId)) {
            throw IllegalStateException("이미 신고한 콘텐츠입니다.")
        }
        val list = restaurantListRepository.findById(listId).orElseThrow { ReportNotFoundException(listId) }

        val rule = ruleEngine.evaluate(
            reason = request.reason,
            content = "${list.title} ${list.description}",
            reporterReportCount = reportRepository.countByReporterId(reporterId),
            targetReportCount = reportRepository.countByRestaurantListIdAndStatusIn(listId, PENDING_STATUSES),
        )
        val report = Report.of(
            targetType = ReportTargetType.RESTAURANT_LIST,
            targetIds = Triple(null, null, list.id),
            reporterId = reporterId,
            reportedUserId = list.user.id,
            reason = request.reason,
            detail = request.detail,
        )
        report.applyRuleScore(rule.score, rule.matchedRules)
        return ReportResponse.from(reportRepository.save(report))
    }

    /** 대기열 개수 (에이전트/어드민용) */
    @Transactional(readOnly = true)
    fun pendingCount(): Long = reportRepository.countByStatus(com.whattoeat.domain.report.entity.ReportStatus.PENDING)

    companion object {
        private val PENDING_STATUSES = listOf(
            com.whattoeat.domain.report.entity.ReportStatus.PENDING,
            com.whattoeat.domain.report.entity.ReportStatus.RESTORED,
        )

        @Suppress("unused")
        private val UNUSED_REASON = ReportReason.OTHER
    }
}
