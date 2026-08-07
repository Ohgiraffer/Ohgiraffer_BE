package com.ohgiraffer.approval.application.query;

import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalListItemResult(
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
}