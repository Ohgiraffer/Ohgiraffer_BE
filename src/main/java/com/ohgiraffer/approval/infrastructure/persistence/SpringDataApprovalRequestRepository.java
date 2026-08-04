package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataApprovalRequestRepository
        extends JpaRepository<ApprovalRequestJpaEntity, Long> {
}