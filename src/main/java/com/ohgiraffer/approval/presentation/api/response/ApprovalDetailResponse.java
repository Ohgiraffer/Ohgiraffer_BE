package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.ApprovalDetailResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.model.approval.ApprovalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApprovalDetailResponse(
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

    public static ApprovalDetailResponse from(
            ApprovalDetailResult result
    ) {
        return new ApprovalDetailResponse(
                result.approvalId(),
                result.requestType(),
                result.status(),
                result.title(),
                result.reason(),
                result.rejectionReason(),
                result.requesterId(),
                result.requesterName(),
                result.approverId(),
                result.approverName(),
                result.requestedAt(),
                result.confirmedAt(),
                result.processedAt(),
                result.startDate(),
                result.endDate(),
                result.budgetCategoryId(),
                result.budgetCategoryName(),
                result.itemName(),
                result.amount(),
                result.signatureImage()
        );
    }
}