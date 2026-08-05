package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;

import java.util.Optional;

public interface ApprovalPurchaseDetailRepository {

    ApprovalPurchaseDetail save(
            ApprovalPurchaseDetail approvalPurchaseDetail
    );

    Optional<ApprovalPurchaseDetail> findByApprovalId(
            Long approvalId
    );
}