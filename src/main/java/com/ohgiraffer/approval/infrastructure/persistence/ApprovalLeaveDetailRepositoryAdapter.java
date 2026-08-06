package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalLeaveDetail;
import com.ohgiraffer.approval.domain.repository.ApprovalLeaveDetailRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ApprovalLeaveDetailRepositoryAdapter
        implements ApprovalLeaveDetailRepository {

    private final SpringDataApprovalLeaveDetailRepository repository;

    public ApprovalLeaveDetailRepositoryAdapter(
            SpringDataApprovalLeaveDetailRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ApprovalLeaveDetail save(
            ApprovalLeaveDetail approvalLeaveDetail
    ) {
        ApprovalLeaveDetailJpaEntity entity =
                ApprovalLeaveDetailJpaEntity.from(
                        approvalLeaveDetail
                );

        ApprovalLeaveDetailJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<ApprovalLeaveDetail> findByApprovalId(
            Long approvalId
    ) {
        return repository
                .findByApprovalId(
                        approvalId
                )
                .map(
                        ApprovalLeaveDetailJpaEntity::toDomain
                );
    }

    @Override
    public List<ApprovalLeaveDetail> findByApprovalIdIn(
            List<Long> approvalIds
    ) {
        if (approvalIds == null || approvalIds.isEmpty()) {
            return List.of();
        }

        return repository
                .findByApprovalIdIn(
                        approvalIds
                )
                .stream()
                .map(
                        ApprovalLeaveDetailJpaEntity::toDomain
                )
                .toList();
    }
}