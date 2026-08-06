package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.result.ApprovalHistoryListResult;

import java.util.List;

public record ApprovalHistoryListResponse(
        List<ApprovalHistoryItemResponse> histories
) {

    public static ApprovalHistoryListResponse from(
            ApprovalHistoryListResult result
    ) {
        return new ApprovalHistoryListResponse(
                result.histories()
                        .stream()
                        .map(
                                ApprovalHistoryItemResponse::from
                        )
                        .toList()
        );
    }
}