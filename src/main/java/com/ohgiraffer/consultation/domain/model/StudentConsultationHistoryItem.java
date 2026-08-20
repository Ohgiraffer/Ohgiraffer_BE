package com.ohgiraffer.consultation.domain.model;

import java.time.LocalDateTime;

public record StudentConsultationHistoryItem(
        Long consultationId,
        String topic,
        LocalDateTime scheduledAt,
        String counselorName,
        ConsultationStatus status
) {
}