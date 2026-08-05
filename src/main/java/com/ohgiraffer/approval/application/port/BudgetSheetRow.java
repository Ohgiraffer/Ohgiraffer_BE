package com.ohgiraffer.approval.application.port;

import java.math.BigDecimal;

public record BudgetSheetRow(
        String categoryName,
        BigDecimal totalAmount,
        BigDecimal usedAmount,
        BigDecimal remainingAmount
) {
}