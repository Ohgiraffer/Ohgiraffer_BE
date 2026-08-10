package com.ohgiraffer.notification.presentation.api.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/*
 * comment.
 *  알림 선택 삭제 요청
 */

public record NotificationBulkDeleteRequest(
        @NotEmpty List<Long> notificationIds
) {
}
