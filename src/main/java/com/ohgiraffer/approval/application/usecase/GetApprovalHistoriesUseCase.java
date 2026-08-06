package com.ohgiraffer.approval.application.usecase;

import com.ohgiraffer.approval.application.query.GetApprovalHistoriesQuery;
import com.ohgiraffer.approval.application.result.ApprovalHistoryListResult;

public interface GetApprovalHistoriesUseCase {

    ApprovalHistoryListResult getHistories(
            GetApprovalHistoriesQuery query
    );
}