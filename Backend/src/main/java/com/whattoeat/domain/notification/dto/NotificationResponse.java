package com.whattoeat.domain.notification.dto;

import com.whattoeat.domain.notification.entity.Notification;
import com.whattoeat.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long actorId,
        String actorNickname,
        Long feedId,
        NotificationType type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {
    public NotificationResponse(Notification notification) {
        this(
                notification.getId(),
                notification.getActor().getId(),
                notification.getActor().getNickname(),
                notification.getFeed() != null ? notification.getFeed().getId() : null,
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
