package com.whattoeat.domain.report.controller

import com.whattoeat.domain.report.dto.ReportCreateRequest
import com.whattoeat.domain.report.dto.ReportResponse
import com.whattoeat.domain.report.service.ReportService
import com.whattoeat.global.rsData.RsData
import com.whattoeat.global.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService,
) {

    /** 피드 신고 — 로그인(익명 계정 포함)한 회원 누구나 */
    @PostMapping("/feeds/{feedId}")
    @PreAuthorize("isAuthenticated()")
    fun reportFeed(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable feedId: Long,
        @Valid @RequestBody request: ReportCreateRequest,
    ): RsData<ReportResponse> {
        val report = reportService.reportFeed(requireNotNull(userDetails.user.id), feedId, request)
        return RsData.success(report, "신고가 접수되었습니다. 검토 후 처리됩니다.")
    }

    /** 댓글 신고 */
    @PostMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    fun reportComment(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable commentId: Long,
        @Valid @RequestBody request: ReportCreateRequest,
    ): RsData<ReportResponse> {
        val report = reportService.reportComment(requireNotNull(userDetails.user.id), commentId, request)
        return RsData.success(report, "신고가 접수되었습니다. 검토 후 처리됩니다.")
    }

    /** 맛집 리스트 신고 */
    @PostMapping("/lists/{listId}")
    @PreAuthorize("isAuthenticated()")
    fun reportRestaurantList(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable listId: Long,
        @Valid @RequestBody request: ReportCreateRequest,
    ): RsData<ReportResponse> {
        val report = reportService.reportRestaurantList(requireNotNull(userDetails.user.id), listId, request)
        return RsData.success(report, "신고가 접수되었습니다. 검토 후 처리됩니다.")
    }
}
