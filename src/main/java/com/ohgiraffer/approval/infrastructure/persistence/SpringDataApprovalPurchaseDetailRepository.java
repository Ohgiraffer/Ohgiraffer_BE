package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataApprovalPurchaseDetailRepository
        extends JpaRepository<ApprovalPurchaseDetailJpaEntity, Long> {

    Optional<ApprovalPurchaseDetailJpaEntity> findByApprovalId(
            Long approvalId
    );

    List<ApprovalPurchaseDetailJpaEntity> findByApprovalIdIn(
            List<Long> approvalIds
    );
}