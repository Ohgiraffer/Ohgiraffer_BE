package com.ohgiraffer.notification.domain.listener;

import com.ohgiraffer.notification.application.command.NotificationCreateCommand;
import com.ohgiraffer.notification.application.usecase.NotificationCommandUseCase;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * comment.
 *  NotificationRequestedEvent 리스너
 *  AFTER_COMMIT에서만 처리 - 발행 측(결재 승인 등) 트랜잭션이 실제로 커밋된 뒤에만 알림 생성
 *  롤백된 트랜잭션에서 발행된 이벤트는 이 리스너까지 도달하지 않음
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationCommandUseCase notificationCommandUseCase;

    // 이벤트를 커맨드로 변환해서 기존 생성 로직(row 저장 + notificationOn 체크 후 SSE push) 그대로 재사용
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationRequestedEvent event) {
        try {
            notificationCommandUseCase.create(new NotificationCreateCommand(
                    event.userId(),
                    event.notificationType(),
                    event.title(),
                    event.content(),
                    event.relatedEntityType(),
                    event.relatedEntityId()
            ));
        } catch (Exception e) {
            // 알림 생성 실패가 원본 도메인(결재 등)의 트랜잭션에 영향을 주면 안 됨 - 여기서 흡수하고 로그만 남김
            log.error("[Notification] 이벤트 기반 알림 생성 실패 | userId={}, type={}",
                    event.userId(), event.notificationType(), e);
        }
    }

}
