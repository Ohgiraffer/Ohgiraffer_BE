package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.application.usecase.ConsultationQueryUsecase;

public record CounselorResponse(
        Long counselorId,
        String name,
        String role
) {
    public static CounselorResponse from(ConsultationQueryUsecase.CounselorInfo info) {
        return new CounselorResponse(info.counselorId(), info.name(), info.role());
    }
}