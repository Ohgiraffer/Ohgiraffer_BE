package com.ohgiraffer.approval.presentation.api.request;

import java.math.BigDecimal;

public record CreatePurchaseApprovalRequest(
        Long approverId,
        Long budgetCategoryId,
        String itemName,
        BigDecimal amount,
        String reason,
        Long signatureId
) {
}