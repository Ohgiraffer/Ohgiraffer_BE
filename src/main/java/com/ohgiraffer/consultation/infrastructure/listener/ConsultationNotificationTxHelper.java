package com.ohgiraffer.consultation.infrastructure.listener;

import com.ohgiraffer.consultation.domain.event.ConsultationRequestedEvent;
import com.ohgiraffer.notification.domain.event.NotificationRequestedEvent;
import com.ohgiraffer.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ConsultationNotificationTxHelper {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishInNewTransaction(ConsultationRequestedEvent event) {
        eventPublisher.publishEvent(new NotificationRequestedEvent(
                event.counselorId(),
                NotificationType.CONSULTATION,
                "상담 신청이 접수되었습니다",
                "\"" + event.topic() + "\" 주제로 상담이 신청되었습니다.",
                "CONSULTATION",
                event.consultationId()
        ));
    }

}
