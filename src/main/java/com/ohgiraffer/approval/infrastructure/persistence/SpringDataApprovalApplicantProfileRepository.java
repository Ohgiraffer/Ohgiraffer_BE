package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataApprovalApplicantProfileRepository
        extends JpaRepository<ApprovalApplicantProfileJpaEntity, Long> {

    Optional<ApprovalApplicantProfileJpaEntity> findByUserId(
            Long userId
    );
}