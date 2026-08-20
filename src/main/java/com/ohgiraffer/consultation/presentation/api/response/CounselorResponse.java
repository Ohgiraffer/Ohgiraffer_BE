package com.ohgiraffer.consultation.presentation.api.response;

import com.ohgiraffer.consultation.domain.model.CounselorInfo;
import com.ohgiraffer.user.domain.model.Role;

public record CounselorResponse(
        Long counselorId,
        String name,
        Role role,
        String profileImgUrl
) {
    public static CounselorResponse from(CounselorInfo info) {
        return new CounselorResponse(info.counselorId(), info.name(), info.role(), info.profileImgUrl());
    }
}