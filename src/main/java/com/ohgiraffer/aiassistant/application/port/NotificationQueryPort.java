package com.ohgiraffer.aiassistant.application.port;

import com.ohgiraffer.notification.application.result.NotificationResult;

import java.util.List;

/* comment.
 *  알림 도메인 조회 Port - AI비서 전용
 *  Adapter 내부에서 NotificationQueryUseCase(자체 도메인)를 직접 주입받아 위임
 */

public interface NotificationQueryPort {

    // 안읽음 알림만 반환
    List<NotificationResult> getUnreadNotifications(Long userId);

}
