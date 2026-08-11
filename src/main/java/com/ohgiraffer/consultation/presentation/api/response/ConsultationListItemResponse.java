package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.ConsultationListItem;
import com.ohgiraffer.consultation.domain.model.ConsultationStatus;

import java.time.LocalDateTime;

public record ConsultationListItemResponse(
        Long consultationId,
        String topic,
        LocalDateTime scheduledAt,
        String requesterName,
        String counselorName,
        ConsultationStatus status
) {
    public static ConsultationListItemResponse from(ConsultationListItem item) {
        return new ConsultationListItemResponse(
                item.consultationId(),
                item.topic(),
                item.scheduledAt(),
                item.requesterName(),
                item.counselorName(),
                item.status()
        );
    }
}