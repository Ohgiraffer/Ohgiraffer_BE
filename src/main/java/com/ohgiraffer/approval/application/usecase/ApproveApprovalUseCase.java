package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface ApproveApprovalUseCase {

    CreateApprovalResult approve(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    );
}