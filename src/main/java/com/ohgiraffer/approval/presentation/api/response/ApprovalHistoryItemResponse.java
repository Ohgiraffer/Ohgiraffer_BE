package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.result.ApprovalHistoryItemResult;

import java.time.LocalDateTime;

public record ApprovalHistoryItemResponse(
        Long approvalHistoryId,
        Long approvalId,
        Long changedBy,
        String fieldName,
        String oldValue,
        String newValue,
        String note,
        LocalDateTime changedAt
) {

    public static ApprovalHistoryItemResponse from(
            ApprovalHistoryItemResult result
    ) {
        return new ApprovalHistoryItemResponse(
                result.approvalHistoryId(),
                result.approvalId(),
                result.changedBy(),
                result.fieldName(),
                result.oldValue(),
                result.newValue(),
                result.note(),
                result.changedAt()
        );
    }
}