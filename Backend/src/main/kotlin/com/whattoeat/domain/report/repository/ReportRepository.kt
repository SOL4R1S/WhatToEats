package com.whattoeat.domain.report.repository

import com.whattoeat.domain.report.entity.Report
import com.whattoeat.domain.report.entity.ReportStatus
import com.whattoeat.domain.report.entity.ReportTargetType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long> {

    /** 동일인의 동일 대상 중복 신고 방지 */
    fun existsByReporterIdAndTargetTypeAndFeedId(
        reporterId: Long,
        targetType: ReportTargetType,
        feedId: Long,
    ): Boolean

    fun existsByReporterIdAndTargetTypeAndCommentId(
        reporterId: Long,
        targetType: ReportTargetType,
        commentId: Long,
    ): Boolean

    fun existsByReporterIdAndTargetTypeAndRestaurantListId(
        reporterId: Long,
        targetType: ReportTargetType,
        restaurantListId: Long,
    ): Boolean

    /** 에이전트 검토 대기열: PENDING을 오래된 순으로 (선착순 처리) */
    fun findByStatusOrderByIdAsc(status: ReportStatus, pageable: Pageable): List<Report>

    /** 처리 완료 이력 (최근 순, 어드민 페이지의 삭제내역 화면) */
    fun findByStatusInOrderByResolvedAtDesc(statuses: Collection<ReportStatus>, pageable: Pageable): List<Report>

    fun countByStatus(status: ReportStatus): Long

    // ---- 룰 엔진 참고용 카운트 ----
    fun countByReporterId(reporterId: Long): Long

    fun countByFeedIdAndStatusIn(feedId: Long, statuses: Collection<ReportStatus>): Long

    fun countByCommentIdAndStatusIn(commentId: Long, statuses: Collection<ReportStatus>): Long

    fun countByRestaurantListIdAndStatusIn(restaurantListId: Long, statuses: Collection<ReportStatus>): Long
}
