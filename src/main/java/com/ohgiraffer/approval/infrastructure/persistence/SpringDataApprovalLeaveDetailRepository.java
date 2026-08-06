package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataApprovalLeaveDetailRepository
        extends JpaRepository<ApprovalLeaveDetailJpaEntity, Long> {

    Optional<ApprovalLeaveDetailJpaEntity> findByApprovalId(
            Long approvalId
    );

    List<ApprovalLeaveDetailJpaEntity> findByApprovalIdIn(
            List<Long> approvalIds
    );
}