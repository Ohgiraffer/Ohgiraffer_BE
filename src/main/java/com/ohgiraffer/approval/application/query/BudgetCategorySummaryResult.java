package com.ohgiraffer.approval.application.query;

import java.math.BigDecimal;

public record BudgetCategorySummaryResult(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        BigDecimal usageRate
) {
}