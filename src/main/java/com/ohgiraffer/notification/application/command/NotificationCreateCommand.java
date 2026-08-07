package com.ohgiraffer.notification.application.command;

import com.ohgiraffer.notification.domain.model.NotificationType;

/*
 * comment.
 *  알림 생성 커맨드
 *  외부 미노출, 각 도메인(전자결재/공지/캘린더/출결/상담/제출/채팅)에서 내부 호출 시 사용
 */

public record NotificationCreateCommand(
        Long userId,
        NotificationType notificationType,
        String title,
        String content,
        String relatedEntityType,
        Long relatedEntityId
) {
}
