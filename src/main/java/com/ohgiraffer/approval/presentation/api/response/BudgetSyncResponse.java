package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;

import java.time.LocalDateTime;

public record BudgetSyncResponse(
        int syncedCount,
        LocalDateTime syncedAt
) {

    public static BudgetSyncResponse from(
            BudgetSyncResult result
    ) {
        return new BudgetSyncResponse(
                result.syncedCategoryCount(),
                result.lastSyncedAt()
        );
    }
}