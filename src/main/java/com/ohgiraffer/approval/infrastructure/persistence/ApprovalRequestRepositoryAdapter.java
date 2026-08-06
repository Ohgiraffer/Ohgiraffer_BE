package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalRequest;
import com.ohgiraffer.approval.domain.model.approval.ApprovalStatus;
import com.ohgiraffer.approval.domain.repository.ApprovalRequestRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public List<ApprovalRequest> findByRequesterIdOrderByRequestedAtDesc(
            Long requesterId
    ) {
        return repository
                .findByRequesterIdOrderByRequestedAtDesc(
                        requesterId
                )
                .stream()
                .map(
                        ApprovalRequestJpaEntity::toDomain
                )
                .toList();
    }

    @Override
    public List<ApprovalRequest> findProcessingApprovals(
            Long userId,
            Long bootcampId
    ) {
        return repository
                .findProcessingApprovals(
                        userId,
                        bootcampId,
                        ApprovalStatus.PENDING
                )
                .stream()
                .map(
                        ApprovalRequestJpaEntity::toDomain
                )
                .toList();
    }

    @Override
    public int checkPendingApproval(
            Long approvalId,
            Long approverId,
            LocalDateTime confirmedAt
    ) {
        return repository.checkPendingApproval(
                approvalId,
                approverId,
                confirmedAt,
                ApprovalStatus.PENDING,
                ApprovalStatus.CHECKED
        );
    }
}