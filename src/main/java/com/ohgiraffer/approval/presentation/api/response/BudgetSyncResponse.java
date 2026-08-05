package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.usecase.BudgetSyncResult;

import java.time.LocalDateTime;

public record BudgetSyncResponse(
        int syncedCategoryCount,
        LocalDateTime lastSyncedAt
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