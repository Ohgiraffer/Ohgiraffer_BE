package com.ohgiraffer.approval.application.command;

import java.math.BigDecimal;

public record CreatePurchaseApprovalCommand(
        Long requesterId,
        Long budgetCategoryId,
        String itemName,
        BigDecimal amount,
        String reason
) {
}