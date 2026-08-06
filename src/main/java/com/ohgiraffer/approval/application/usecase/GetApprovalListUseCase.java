package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.ApprovalListItemResult;
import com.ohgiraffer.approval.application.query.ApprovalListScope;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetApprovalListUseCase {

    List<ApprovalListItemResult> getApprovals(
            Long loginUserId,
            Role loginUserRole,
            ApprovalListScope scope
    );
}