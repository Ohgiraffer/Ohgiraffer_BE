package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.ApprovalDetailResult;
import com.ohgiraffer.user.domain.model.Role;

public interface GetApprovalDetailUseCase {

    ApprovalDetailResult getApprovalDetail(
            Long loginUserId,
            Role loginUserRole,
            Long approvalId
    );
}