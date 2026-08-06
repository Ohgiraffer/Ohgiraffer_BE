package com.ohgiraffer.approval.application.result;

import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;

import java.time.LocalDateTime;

public record ApprovalHistoryItemResult(
        Long approvalHistoryId,
        Long approvalId,
        Long changedBy,
        String fieldName,
        String oldValue,
        String newValue,
        String note,
        LocalDateTime changedAt
) {

    public static ApprovalHistoryItemResult from(
            ApprovalHistory approvalHistory
    ) {
        return new ApprovalHistoryItemResult(
                approvalHistory.getId(),
                approvalHistory.getApprovalId(),
                approvalHistory.getChangedBy(),
                approvalHistory.getFieldName(),
                approvalHistory.getOldValue(),
                approvalHistory.getNewValue(),
                approvalHistory.getNote(),
                approvalHistory.getChangedAt()
        );
    }
}