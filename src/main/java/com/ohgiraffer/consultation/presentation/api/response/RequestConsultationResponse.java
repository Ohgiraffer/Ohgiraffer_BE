package com.ohgiraffer.consultation.presentation.api.response;

public record RequestConsultationResponse(
        Long consultationId
) {
    public static RequestConsultationResponse from(Long consultationId) {
        return new RequestConsultationResponse(consultationId);
    }
}