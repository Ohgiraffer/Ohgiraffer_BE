package com.ohgiraffer.approval.application.query;

import com.ohgiraffer.user.domain.model.Role;

public record GetApprovalHistoriesQuery(
        Long approvalId,
        Long loginUserId,
        Role loginUserRole
) {
}