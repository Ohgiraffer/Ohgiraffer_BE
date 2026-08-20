package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.ConsultationDetail;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;

import java.time.LocalDateTime;

public record ConsultationDetailResponse(
        Long consultationId,
        String topic,
        String requesterName,
        String counselorName,
        LocalDateTime scheduledAt,
        String content,
        String counselorNote,
        String aiBrief,
        ConsultationStatus status
) {
    public static ConsultationDetailResponse from(ConsultationDetail detail) {
        return new ConsultationDetailResponse(
                detail.consultationId(),
                detail.topic(),
                detail.requesterName(),
                detail.counselorName(),
                detail.scheduledAt(),
                detail.content(),
                detail.counselorNote(),
                detail.aiBrief(),
                detail.status()
        );
    }
}