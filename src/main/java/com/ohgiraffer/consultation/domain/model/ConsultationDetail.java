package com.ohgiraffer.consultation.domain.model;

import java.time.LocalDateTime;

public record ConsultationDetail(
        Long consultationId,
        String topic,
        String requesterName,
        String counselorName,
        LocalDateTime scheduledAt,
        String content,
        String counselorNote,
        ConsultationStatus status
) {
}
