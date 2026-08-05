package com.ohgiraffer.approval.domain.model.approval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalHistory {

    private static final String STATUS_FIELD_NAME = "status";

    private final Long id;
    private final Long approvalId;
    private final Long changedBy;
    private final String fieldName;
    private final String oldValue;
    private final String newValue;
    private final String note;
    private final LocalDateTime changedAt;

    public static ApprovalHistory created(
            Long approvalId,
            Long changedBy,
            ApprovalStatus newStatus,
            LocalDateTime changedAt
    ) {
        return new ApprovalHistory(
                null,
                approvalId,
                changedBy,
                STATUS_FIELD_NAME,
                null,
                newStatus.name(),
                "휴가 신청 생성",
                changedAt
        );
    }

    public static ApprovalHistory statusChanged(
            Long approvalId,
            Long changedBy,
            ApprovalStatus oldStatus,
            ApprovalStatus newStatus,
            String note,
            LocalDateTime changedAt
    ) {
        return new ApprovalHistory(
                null,
                approvalId,
                changedBy,
                STATUS_FIELD_NAME,
                oldStatus.name(),
                newStatus.name(),
                note,
                changedAt
        );
    }

    public static ApprovalHistory restore(
            Long id,
            Long approvalId,
            Long changedBy,
            String fieldName,
            String oldValue,
            String newValue,
            String note,
            LocalDateTime changedAt
    ) {
        return new ApprovalHistory(
                id,
                approvalId,
                changedBy,
                fieldName,
                oldValue,
                newValue,
                note,
                changedAt
        );
    }
}