package com.ohgiraffer.approval.domain.model.budget;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BudgetAllocation {

    private Long id;
    private Long budgetCategoryId;
    private BigDecimal totalAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime lastSyncedAt;

    public static BudgetAllocation create(
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        return new BudgetAllocation(
                null,
                budgetCategoryId,
                totalAmount,
                usedAmount,
                remainingAmount,
                lastSyncedAt
        );
    }

    public static BudgetAllocation restore(
            Long id,
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        return new BudgetAllocation(
                id,
                budgetCategoryId,
                totalAmount,
                usedAmount,
                remainingAmount,
                lastSyncedAt
        );
    }

    public BudgetAllocation updateAmounts(
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        this.totalAmount = totalAmount;
        this.usedAmount = usedAmount;
        this.remainingAmount = remainingAmount;
        this.lastSyncedAt = lastSyncedAt;

        return this;
    }
}