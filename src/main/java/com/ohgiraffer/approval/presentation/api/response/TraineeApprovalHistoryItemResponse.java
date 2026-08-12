package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryItemResult;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;

import java.time.LocalDate;

public record TraineeApprovalHistoryItemResponse(
        Long approvalId,
        LocalDate requestedDate,
        String typeName,
        LocalDate startDate,
        LocalDate endDate,
        Long leaveDays,
        String period,
        LocalDate approvedDate,
        ApprovalStatus status
) {

    public static TraineeApprovalHistoryItemResponse from(
            TraineeApprovalHistoryItemResult result
    ) {
        return new TraineeApprovalHistoryItemResponse(
                result.approvalId(),
                result.requestedDate(),
                result.typeName(),
                result.startDate(),
                result.endDate(),
                result.leaveDays(),
                createPeriod(
                        result
                ),
                result.approvedDate(),
                result.status()
        );
    }

    private static String createPeriod(
            TraineeApprovalHistoryItemResult result
    ) {
        if (result.startDate() == null || result.endDate() == null) {
            return "";
        }

        return result.startDate()
                + " ~ "
                + result.endDate()
                + "("
                + result.leaveDays()
                + "일)";
    }
}