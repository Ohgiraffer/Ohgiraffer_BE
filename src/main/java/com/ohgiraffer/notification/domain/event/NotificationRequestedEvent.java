package com.ohgiraffer.notification.domain.event;

import com.ohgiraffer.notification.domain.model.NotificationType;

/*
 * comment.
 *  알림 생성 트리거 이벤트
 *  전자결재/공지/캘린더/출결/상담/제출/채팅 등 다른 도메인이 알림을 발생시킬 때 이 이벤트를 발행함
 *  알림 도메인이 NotificationCommandUseCase를 직접 의존하지 않아도 되도록 결합도를 낮추는 용도
 */

public record NotificationRequestedEvent(
        Long userId,
        NotificationType notificationType,
        String title,
        String content,
        String relatedEntityType,
        Long relatedEntityId
) {
}
