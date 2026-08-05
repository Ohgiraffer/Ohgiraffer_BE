package com.ohgiraffer.approval.application.usecase;

import java.time.LocalDateTime;

public record BudgetSyncResult(
        int syncedCategoryCount,
        LocalDateTime lastSyncedAt
) {
}