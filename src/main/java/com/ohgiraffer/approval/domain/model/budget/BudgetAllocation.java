package com.ohgiraffer.approval.domain.model.budget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BudgetAllocation {

    private final Long id;
    private final Long budgetCategoryId;
    private BigDecimal totalAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDateTime lastSyncedAt;

    public static BudgetAllocation create(
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        BudgetAllocation budgetAllocation =
                new BudgetAllocation(
                        null,
                        budgetCategoryId
                );

        budgetAllocation.updateAmounts(
                totalAmount,
                usedAmount,
                remainingAmount,
                lastSyncedAt
        );

        return budgetAllocation;
    }

    public static BudgetAllocation restore(
            Long id,
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDateTime lastSyncedAt
    ) {
        BudgetAllocation budgetAllocation =
                new BudgetAllocation(
                        id,
                        budgetCategoryId
                );

        budgetAllocation.totalAmount = totalAmount;
        budgetAllocation.usedAmount = usedAmount;
        budgetAllocation.remainingAmount = remainingAmount;
        budgetAllocation.periodStart = periodStart;
        budgetAllocation.periodEnd = periodEnd;
        budgetAllocation.lastSyncedAt = lastSyncedAt;

        return budgetAllocation;
    }

    public void updateAmounts(
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        this.totalAmount = totalAmount;
        this.usedAmount = usedAmount;
        this.remainingAmount = remainingAmount;
        this.lastSyncedAt = lastSyncedAt;
    }

    public BigDecimal calculateUsageRate() {
        if (totalAmount == null
                || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return usedAmount
                .multiply(
                        BigDecimal.valueOf(
                                100
                        )
                )
                .divide(
                        totalAmount,
                        2,
                        java.math.RoundingMode.HALF_UP
                );
    }
}