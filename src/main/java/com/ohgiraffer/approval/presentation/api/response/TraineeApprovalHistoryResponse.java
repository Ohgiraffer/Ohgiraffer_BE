package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.TraineeApprovalHistoryResult;

import java.util.List;

public record TraineeApprovalHistoryResponse(
        List<TraineeApprovalHistoryItemResponse> approvals
) {

    public static TraineeApprovalHistoryResponse from(
            TraineeApprovalHistoryResult result
    ) {
        return new TraineeApprovalHistoryResponse(
                result.approvals()
                        .stream()
                        .map(
                                TraineeApprovalHistoryItemResponse::from
                        )
                        .toList()
        );
    }
}