package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;

import java.time.LocalDate;

public record ApprovalProfileResponse(
        LocalDate birthDate,
        String phoneNumber
) {

    public static ApprovalProfileResponse from(
            ApprovalProfileResult result
    ) {
        return new ApprovalProfileResponse(
                result.birthDate(),
                result.phoneNumber()
        );
    }
}