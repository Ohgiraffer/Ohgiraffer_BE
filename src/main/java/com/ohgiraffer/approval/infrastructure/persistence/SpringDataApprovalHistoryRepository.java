package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataApprovalHistoryRepository
        extends JpaRepository<ApprovalHistoryJpaEntity, Long> {

    List<ApprovalHistoryJpaEntity> findAllByApprovalIdOrderByChangedAtAsc(
            Long approvalId
    );
}