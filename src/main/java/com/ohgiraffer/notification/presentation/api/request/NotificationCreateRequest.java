package com.ohgiraffer.notification.presentation.api.request;

import com.ohgiraffer.notification.domain.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
 * comment.
 *  알림 생성 요청 - 내부 도메인 간 호출 전용, 외부 미노출
 */

public record NotificationCreateRequest(
        @NotNull Long userId,
        @NotNull NotificationType notificationType,
        @NotNull @Size(max = 100) String title,
        @NotNull String content,
        String relatedEntityType,
        Long relatedEntityId
) {
}
