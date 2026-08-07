package com.ohgiraffer.notification.application.usecase;

import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.domain.model.NotificationType;

import java.util.List;

/*
 * comment.
 *  알림 조회 유스케이스
 *  CQRS 상 Query 담당 - 상태 변경 없는 조회 전용
 */

public interface NotificationQueryUseCase {

    // 목록 조회 - isRead/notificationType은 필터 미전달 시 null, 페이징 없이 전체 반환
    List<NotificationResult> getNotifications(Long userId, Boolean isRead, NotificationType notificationType);

    // 안읽음 개수 조회
    long getUnreadCount(Long userId);

}
