package com.ohgiraffer.approval.domain.model.approval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApprovalPurchaseDetail {

    private final Long id;
    private final Long approvalId;
    private final Long budgetCategoryId;
    private String itemName;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ApprovalPurchaseDetail create(
            Long approvalId,
            Long budgetCategoryId,
            String itemName,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {
        ApprovalPurchaseDetail purchaseDetail =
                new ApprovalPurchaseDetail(
                        null,
                        approvalId,
                        budgetCategoryId
                );

        purchaseDetail.itemName = itemName;
        purchaseDetail.amount = amount;
        purchaseDetail.createdAt = createdAt;

        return purchaseDetail;
    }

    public static ApprovalPurchaseDetail restore(
            Long id,
            Long approvalId,
            Long budgetCategoryId,
            String itemName,
            BigDecimal amount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        ApprovalPurchaseDetail purchaseDetail =
                new ApprovalPurchaseDetail(
                        id,
                        approvalId,
                        budgetCategoryId
                );

        purchaseDetail.itemName = itemName;
        purchaseDetail.amount = amount;
        purchaseDetail.createdAt = createdAt;
        purchaseDetail.updatedAt = updatedAt;

        return purchaseDetail;
    }
}