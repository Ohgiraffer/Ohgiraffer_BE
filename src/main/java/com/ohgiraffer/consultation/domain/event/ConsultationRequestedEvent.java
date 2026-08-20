package com.ohgiraffer.consultation.domain.event;

import java.time.LocalDateTime;

public record ConsultationRequestedEvent(
        Long consultationId,
        Long counselorId,
        Long requesterId,
        String topic,
        LocalDateTime scheduledAt
) {
}