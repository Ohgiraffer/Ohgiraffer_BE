package com.ohgiraffer.consultation.infrastructure.listener;

import com.ohgiraffer.consultation.domain.event.ConsultationRequestedEvent;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * comment.
 *  ConsultationRequestedEvent 리스너.
 *  상담 신청 트랜잭션이 커밋된 뒤(AFTER_COMMIT), 담당 강사·매니저(counselorId)에게
 *  알림 도메인의 NotificationRequestedEvent를 재발행한다.
 *  상담 도메인 자체는 알림 도메인을 모르게 유지하기 위해 이 변환 책임을 여기서 흡수한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationNotificationListener {

    private final ApplicationEventPublisher eventPublisher;
    private final ConsultationNotificationTxHelper txHelper; // 새로 분리

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ConsultationRequestedEvent event) {
        try {
            txHelper.publishInNewTransaction(event);
        } catch (Exception e) {
            log.error("[Consultation->Notification] 알림 변환 발행 실패 | consultationId={}", event.consultationId(), e);
        }
    }
}
