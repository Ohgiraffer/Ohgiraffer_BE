package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.CounselorInfo;

public record CounselorResponse(
        Long counselorId,
        String name,
        String role
) {
    public static CounselorResponse from(CounselorInfo info) {
        return new CounselorResponse(info.counselorId(), info.name(), info.role());
    }
}