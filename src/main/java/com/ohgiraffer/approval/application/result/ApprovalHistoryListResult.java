package com.ohgiraffer.approval.application.result;

import java.util.List;

public record ApprovalHistoryListResult(
        List<ApprovalHistoryItemResult> histories
) {

    public static ApprovalHistoryListResult from(
            List<ApprovalHistoryItemResult> histories
    ) {
        return new ApprovalHistoryListResult(
                histories
        );
    }
}