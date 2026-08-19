package com.ohgiraffer.consultation.infrastructure.listener;

import com.ohgiraffer.consultation.domain.event.ConsultationRequestedEvent;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
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

    private static final String RELATED_ENTITY_TYPE = "CONSULTATION";

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ConsultationRequestedEvent event) {
        try {
            eventPublisher.publishEvent(new NotificationRequestedEvent(
                    event.counselorId(),
                    NotificationType.CONSULTATION,
                    "상담 신청이 접수되었습니다",
                    "\"" + event.topic() + "\" 주제로 상담이 신청되었습니다.",
                    RELATED_ENTITY_TYPE,
                    event.consultationId()
            ));
        } catch (Exception e) {
            // 알림 실패가 상담 신청 자체를 되돌리면 안 됨 - 흡수하고 로그만 남김
            log.error("[Consultation->Notification] 알림 변환 발행 실패 | consultationId={}", event.consultationId(), e);
        }
    }
}
