package com.whattoeat.domain.notification.controller;

import com.whattoeat.domain.notification.dto.NotificationResponse;
import com.whattoeat.domain.notification.entity.Notification;
import com.whattoeat.domain.notification.service.NotificationService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public RsData<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = userDetails.getUserId();

        Page<Notification> notifications = notificationService.getNotifications(userId, pageable);

        List<NotificationResponse> result = notifications
                .map(NotificationResponse::new)
                .getContent();

        return RsData.success(result, "알림 목록 조회가 완료되었습니다.");
    }

    // 알림 읽음 처리
    @PutMapping("/{id}/read")
    public RsData<NotificationResponse> readNotification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();

        Notification notification = notificationService.markAsRead(userId, id);

        return RsData.success(new NotificationResponse(notification), "알림을 읽음 처리했습니다.");
    }
}
