package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.BudgetAllocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "budget_allocation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UQ_BUDGET_ALLOCATION_CATEGORY",
                        columnNames = "budget_category_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetAllocationJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "budget_allocation_id")
    private Long id;

    @Column(
            name = "budget_category_id",
            nullable = false
    )
    private Long budgetCategoryId;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalAmount;

    @Column(
            name = "used_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal usedAmount;

    @Column(
            name = "remaining_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal remainingAmount;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    private BudgetAllocationJpaEntity(
            Long id,
            Long budgetCategoryId,
            BigDecimal totalAmount,
            BigDecimal usedAmount,
            BigDecimal remainingAmount,
            LocalDateTime lastSyncedAt
    ) {
        this.id = id;
        this.budgetCategoryId = budgetCategoryId;
        this.totalAmount = totalAmount;
        this.usedAmount = usedAmount;
        this.remainingAmount = remainingAmount;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static BudgetAllocationJpaEntity from(
            BudgetAllocation budgetAllocation
    ) {
        return new BudgetAllocationJpaEntity(
                budgetAllocation.getId(),
                budgetAllocation.getBudgetCategoryId(),
                budgetAllocation.getTotalAmount(),
                budgetAllocation.getUsedAmount(),
                budgetAllocation.getRemainingAmount(),
                budgetAllocation.getLastSyncedAt()
        );
    }

    public BudgetAllocation toDomain() {
        return BudgetAllocation.restore(
                id,
                budgetCategoryId,
                totalAmount,
                usedAmount,
                remainingAmount,
                lastSyncedAt
        );
    }
}