package com.ohgiraffer.approval.application.command;

public record BudgetColumnMapping(
        String category,
        String totalAmount,
        String usedAmount,
        String remainingAmount
) {
}