package com.ohgiraffer.approval.application.query;

import java.util.List;

public record TraineeApprovalHistoryResult(
        List<TraineeApprovalHistoryItemResult> approvals
) {
}