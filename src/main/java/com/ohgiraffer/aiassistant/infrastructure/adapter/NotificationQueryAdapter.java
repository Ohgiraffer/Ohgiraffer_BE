package com.ohgiraffer.aiassistant.infrastructure.adapter;

import com.ohgiraffer.aiassistant.application.port.NotificationQueryPort;
import com.ohgiraffer.notification.application.result.NotificationResult;
import com.ohgiraffer.notification.application.usecase.NotificationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/* comment.
 *  NotificationQueryPort 실구현체 - 알림 도메인(자체 소유)의 NotificationQueryUseCase 직접 주입받아 위임
 */

@Component
@RequiredArgsConstructor
public class NotificationQueryAdapter implements NotificationQueryPort {

    private final NotificationQueryUseCase notificationQueryUseCase;

    @Override
    public List<NotificationResult> getUnreadNotifications(Long userId) {
        return notificationQueryUseCase.getNotifications(userId, false, null);  // isRead=false, type 필터 없음
    }

}
