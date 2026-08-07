package com.ohgiraffer.notification.application.result;

import com.ohgiraffer.notification.domain.model.Notification;
import com.ohgiraffer.notification.domain.model.NotificationType;

import java.time.Instant;

/*
 * comment.
 *  알림 응답 결과 - Notification 도메인 모델을 API 응답 형태로 변환
 */

public record NotificationResult(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String content,
        String relatedEntityType,
        Long relatedEntityId,
        boolean isRead,
        Instant createdAt
) {

    public static NotificationResult from(Notification notification) {
        return new NotificationResult(
                notification.getNotificationId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

}
