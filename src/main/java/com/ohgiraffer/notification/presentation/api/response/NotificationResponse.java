package com.ohgiraffer.notification.presentation.api.response;

import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.domain.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String content,
        String relatedEntityType,
        Long relatedEntityId,
        boolean isRead,
        Instant createdAt
) {

    public static NotificationResponse from(NotificationResult result) {
        return new NotificationResponse(
                result.notificationId(),
                result.notificationType(),
                result.title(),
                result.content(),
                result.relatedEntityType(),
                result.relatedEntityId(),
                result.isRead(),
                result.createdAt()
        );
    }

}
