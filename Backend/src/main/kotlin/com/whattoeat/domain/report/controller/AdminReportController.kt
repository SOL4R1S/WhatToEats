package com.whattoeat.domain.report.controller

import com.whattoeat.domain.report.dto.AdminReportView
import com.whattoeat.domain.report.dto.ResolutionRequest
import com.whattoeat.domain.report.service.ReportModerationService
import com.whattoeat.global.rsData.RsData
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 신고 검토·판정 API (어드민 전용).
 *
 * 접근 방식 2가지:
 *  1. ADMIN 권한 JWT (웹 /admin 페이지용)
 *  2. X-Agent-Key 헤더 (헤르메스 에이전트가 cron으로 주기 검토할 때 — 서버 env에만 존재하는 시크릿)
 */
@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN') or @agentKeyGuard.matches(#agentKey)")
class AdminReportController(
    private val moderationService: ReportModerationService,
) {

    /** 검토 대기열 (PENDING, 선착순) */
    @GetMapping("/pending")
    fun pending(
        @RequestHeader("X-Agent-Key", required = false) agentKey: String?,
        @RequestParam(defaultValue = "20") limit: Int,
    ): RsData<List<AdminReportView>> =
        RsData.success(moderationService.pendingQueue(limit), "검토 대기열 조회 완료")

    /** 처리 이력 (삭제내역/기각내역 — 롤백 가능 목록) */
    @GetMapping("/history")
    fun history(
        @RequestHeader("X-Agent-Key", required = false) agentKey: String?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): RsData<List<AdminReportView>> =
        RsData.success(moderationService.history(limit), "처리 이력 조회 완료")

    /** 상태별 카운트 */
    @GetMapping("/stats")
    fun stats(@RequestHeader("X-Agent-Key", required = false) agentKey: String?): RsData<Map<String, Long>> =
        RsData.success(moderationService.counts(), "통계 조회 완료")

    /** 판정: 삭제 확정 (대상 소프트삭제) */
    @PostMapping("/{reportId}/resolve")
    fun resolve(
        @RequestHeader("X-Agent-Key", required = false) agentKey: String?,
        @PathVariable reportId: Long,
        @Valid @RequestBody(required = false) request: ResolutionRequest?,
    ): RsData<AdminReportView> =
        RsData.success(moderationService.resolve(reportId, request?.note), "삭제 처리되었습니다.")

    /** 판정: 기각 (콘텐츠 유지) */
    @PostMapping("/{reportId}/reject")
    fun reject(
        @RequestHeader("X-Agent-Key", required = false) agentKey: String?,
        @PathVariable reportId: Long,
        @Valid @RequestBody(required = false) request: ResolutionRequest?,
    ): RsData<AdminReportView> =
        RsData.success(moderationService.reject(reportId, request?.note), "기각 처리되었습니다.")

    /** 롤백: 삭제 복원 */
    @PostMapping("/{reportId}/restore")
    fun restore(
        @RequestHeader("X-Agent-Key", required = false) agentKey: String?,
        @PathVariable reportId: Long,
        @Valid @RequestBody(required = false) request: ResolutionRequest?,
    ): RsData<AdminReportView> =
        RsData.success(moderationService.restore(reportId, request?.note), "복원되었습니다.")
}
