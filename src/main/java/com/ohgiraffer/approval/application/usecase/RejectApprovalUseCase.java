package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface RejectApprovalUseCase {

    CreateApprovalResult reject(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId,
            String rejectionReason
    );
}