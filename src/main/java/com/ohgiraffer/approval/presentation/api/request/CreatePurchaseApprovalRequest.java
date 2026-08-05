package com.ohgiraffer.approval.presentation.api.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePurchaseApprovalRequest(

        @NotNull
        Long approverId,

        @NotNull
        Long budgetCategoryId,

        @NotBlank
        @Size(max = 100)
        String itemName,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 12, fraction = 2)
        BigDecimal amount,

        @NotBlank
        @Size(max = 1000)
        String reason
) {
}