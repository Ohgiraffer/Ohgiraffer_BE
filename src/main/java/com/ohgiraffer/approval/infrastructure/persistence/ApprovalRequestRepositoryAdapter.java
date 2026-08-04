package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApprovalRequestRepositoryAdapter
        implements ApprovalRequestRepository {

    private final SpringDataApprovalRequestRepository repository;

    public ApprovalRequestRepositoryAdapter(
            SpringDataApprovalRequestRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ApprovalRequest save(
            ApprovalRequest approvalRequest
    ) {
        ApprovalRequestJpaEntity entity =
                ApprovalRequestJpaEntity.from(
                        approvalRequest
                );

        ApprovalRequestJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<ApprovalRequest> findById(
            Long approvalId
    ) {
        return repository
                .findById(
                        approvalId
                )
                .map(
                        ApprovalRequestJpaEntity::toDomain
                );
    }
}