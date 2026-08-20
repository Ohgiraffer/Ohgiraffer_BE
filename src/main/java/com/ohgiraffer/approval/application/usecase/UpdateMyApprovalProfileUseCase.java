package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;

import java.time.LocalDate;

public interface UpdateMyApprovalProfileUseCase {

    ApprovalProfileResult updateProfile(
            Long loginUserId,
            LocalDate birthDate
    );
}