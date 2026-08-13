package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.ApprovalProfileResult;

public interface GetMyApprovalProfileUseCase {

    ApprovalProfileResult getProfile(
            Long loginUserId
    );
}