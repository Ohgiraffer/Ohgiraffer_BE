package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.usecase.CreateApprovalResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.time.LocalDateTime;

public record CreateApprovalResponse(
        Long approvalId,
        Long requesterId,
        Long approverId,
        ApprovalType requestType,
        ApprovalStatus status,
        String title,
        LocalDateTime requestedAt
) {

    public static CreateApprovalResponse from(
            CreateApprovalResult result
    ) {
        return new CreateApprovalResponse(
                result.approvalId(),
                result.requesterId(),
                result.approverId(),
                result.requestType(),
                result.status(),
                result.title(),
                result.requestedAt()
        );
    }
}