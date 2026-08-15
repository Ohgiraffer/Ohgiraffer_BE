package com.ohgiraffer.consultation.presentation.api.response;


import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.model.StudentConsultationHistoryItem;

import java.time.LocalDateTime;

public record StudentConsultationHistoryResponse(
        Long consultationId,
        String topic,
        LocalDateTime scheduledAt,
        String counselorName,
        ConsultationStatus status
) {
    public static StudentConsultationHistoryResponse from(StudentConsultationHistoryItem item) {
        return new StudentConsultationHistoryResponse(
                item.consultationId(),
                item.topic(),
                item.scheduledAt(),
                item.counselorName(),
                item.status()
        );
    }
}
