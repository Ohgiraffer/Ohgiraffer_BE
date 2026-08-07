package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.ApprovalListItemResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalListItemResponse(
        Long approvalId,
        ApprovalType requestType,
        ApprovalStatus status,
        String title,
        Long requesterId,
        String requesterName,
        Long approverId,
        String approverName,
        String budgetCategoryName,
        BigDecimal amount,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime requestedAt
) {

    public static ApprovalListItemResponse from(
            ApprovalListItemResult result
    ) {
        return new ApprovalListItemResponse(
                result.approvalId(),
                result.requestType(),
                result.status(),
                result.title(),
                result.requesterId(),
                result.requesterName(),
                result.approverId(),
                result.approverName(),
                result.budgetCategoryName(),
                result.amount(),
                result.startDate(),
                result.endDate(),
                result.requestedAt()
        );
    }
}