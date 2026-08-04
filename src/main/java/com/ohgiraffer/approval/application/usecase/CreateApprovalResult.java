package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.time.LocalDateTime;

public record CreateApprovalResult(
        Long approvalId,
        Long requesterId,
        Long approverId,
        ApprovalType requestType,
        ApprovalStatus status,
        String title,
        LocalDateTime requestedAt
) {

    public static CreateApprovalResult from(
            ApprovalRequest approvalRequest
    ) {
        return new CreateApprovalResult(
                approvalRequest.getId(),
                approvalRequest.getRequesterId(),
                approvalRequest.getApproverId(),
                approvalRequest.getRequestType(),
                approvalRequest.getStatus(),
                approvalRequest.getTitle(),
                approvalRequest.getRequestedAt()
        );
    }
}