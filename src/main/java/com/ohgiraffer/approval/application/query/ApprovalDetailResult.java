package com.ohgiraffer.approval.application.query;

import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalDetailResult(
        Long approvalId,
        ApprovalType requestType,
        ApprovalStatus status,
        String title,
        String reason,
        String rejectionReason,
        Long requesterId,
        String requesterName,
        Long approverId,
        String approverName,
        LocalDateTime requestedAt,
        LocalDateTime confirmedAt,
        LocalDateTime processedAt,
        LocalDate startDate,
        LocalDate endDate,
        Long budgetCategoryId,
        String budgetCategoryName,
        String itemName,
        BigDecimal amount,
        String signatureImage
) {
}