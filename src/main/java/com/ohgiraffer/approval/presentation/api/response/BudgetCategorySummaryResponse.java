package com.ohgiraffer.approval.presentation.api.response;

import com.ohgiraffer.approval.application.query.BudgetCategorySummaryResult;

import java.math.BigDecimal;

public record BudgetCategorySummaryResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        BigDecimal usageRate
) {

    public static BudgetCategorySummaryResponse from(
            BudgetCategorySummaryResult result
    ) {
        return new BudgetCategorySummaryResponse(
                result.categoryId(),
                result.categoryName(),
                result.totalAmount(),
                result.usedAmount(),
                result.remainingAmount(),
                result.usageRate()
        );
    }
}