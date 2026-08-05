package com.ohgiraffer.approval.application.command;

import java.math.BigDecimal;

public record CreatePurchaseApprovalCommand(
        Long requesterId,
        Long approverId,
        Long budgetCategoryId,
        String itemName,
        BigDecimal amount,
        String reason
) {
}