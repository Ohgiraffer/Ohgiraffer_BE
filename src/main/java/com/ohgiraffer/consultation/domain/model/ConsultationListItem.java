package com.ohgiraffer.consultation.domain.model;

import java.time.LocalDateTime;

public record ConsultationListItem(
        Long consultationId,
        String topic,
        LocalDateTime scheduledAt,
        String requesterName,
        String counselorName,
        ConsultationStatus status
) {
}
