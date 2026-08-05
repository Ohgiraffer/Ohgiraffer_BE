package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_purchase_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalPurchaseDetailJpaEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "purchase_detail_id")
    private Long id;

    @Column(
            name = "approval_id",
            nullable = false
    )
    private Long approvalId;

    @Column(
            name = "budget_category_id",
            nullable = false
    )
    private Long budgetCategoryId;

    @Column(
            name = "item_name",
            nullable = false,
            length = 100
    )
    private String itemName;

    @Column(
            name = "amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private ApprovalPurchaseDetailJpaEntity(
            Long id,
            Long approvalId,
            Long budgetCategoryId,
            String itemName,
            BigDecimal amount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.approvalId = approvalId;
        this.budgetCategoryId = budgetCategoryId;
        this.itemName = itemName;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ApprovalPurchaseDetailJpaEntity from(
            ApprovalPurchaseDetail approvalPurchaseDetail
    ) {
        return new ApprovalPurchaseDetailJpaEntity(
                approvalPurchaseDetail.getId(),
                approvalPurchaseDetail.getApprovalId(),
                approvalPurchaseDetail.getBudgetCategoryId(),
                approvalPurchaseDetail.getItemName(),
                approvalPurchaseDetail.getAmount(),
                approvalPurchaseDetail.getCreatedAt(),
                approvalPurchaseDetail.getUpdatedAt()
        );
    }

    public ApprovalPurchaseDetail toDomain() {
        return ApprovalPurchaseDetail.restore(
                id,
                approvalId,
                budgetCategoryId,
                itemName,
                amount,
                createdAt,
                updatedAt
        );
    }
}