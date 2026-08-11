package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.ConsultationStatus;
import com.ohgiraffer.consultation.domain.model.ConsultationSummary;

import java.time.LocalDateTime;

public record ConsultationSummaryResponse(
        Long consultationId,
        String topic,
        LocalDateTime scheduledAt,
        String counselorName,
        ConsultationStatus status
) {
    public static ConsultationSummaryResponse from(ConsultationSummary summary) {
        return new ConsultationSummaryResponse(
                summary.consultationId(),
                summary.topic(),
                summary.scheduledAt(),
                summary.counselorName(),
                summary.status()
        );
    }
}