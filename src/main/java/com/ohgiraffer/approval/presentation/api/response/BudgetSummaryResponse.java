package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.BudgetSummaryResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BudgetSummaryResponse(
        BigDecimal totalBudgetAmount,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        BigDecimal usageRate,
        LocalDateTime lastSyncedAt,
        List<BudgetCategorySummaryResponse> categories
) {

    public static BudgetSummaryResponse from(
            BudgetSummaryResult result
    ) {
        return new BudgetSummaryResponse(
                result.totalBudgetAmount(),
                result.usedAmount(),
                result.remainingAmount(),
                result.usageRate(),
                result.lastSyncedAt(),
                result.categories()
                        .stream()
                        .map(
                                BudgetCategorySummaryResponse::from
                        )
                        .toList()
        );
    }
}