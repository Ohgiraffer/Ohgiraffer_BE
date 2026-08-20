package com.ohgiraffer.approval.application.query;

import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;

import java.time.LocalDate;

public record TraineeApprovalHistoryItemResult(
        Long approvalId,
        LocalDate requestedDate,
        String typeName,
        Long approverId,
        String approverName,
        LocalDate startDate,
        LocalDate endDate,
        Long leaveDays,
        LocalDate approvedDate,
        ApprovalStatus status
) {
}