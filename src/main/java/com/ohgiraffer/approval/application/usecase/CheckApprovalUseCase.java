package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface CheckApprovalUseCase {

    CreateApprovalResult check(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    );
}