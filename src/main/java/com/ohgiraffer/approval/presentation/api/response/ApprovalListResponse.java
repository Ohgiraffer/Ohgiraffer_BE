package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.ApprovalListItemResult;

import java.util.List;

public record ApprovalListResponse(
        List<ApprovalListItemResponse> approvals
) {

    public static ApprovalListResponse from(
            List<ApprovalListItemResult> results
    ) {
        return new ApprovalListResponse(
                results.stream()
                        .map(
                                ApprovalListItemResponse::from
                        )
                        .toList()
        );
    }
}