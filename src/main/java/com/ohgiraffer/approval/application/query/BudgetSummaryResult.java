package com.ohgiraffer.approval.application.query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BudgetSummaryResult(
        BigDecimal totalBudgetAmount,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        BigDecimal usageRate,
        LocalDateTime lastSyncedAt,
        List<BudgetCategorySummaryResult> categories
) {
}