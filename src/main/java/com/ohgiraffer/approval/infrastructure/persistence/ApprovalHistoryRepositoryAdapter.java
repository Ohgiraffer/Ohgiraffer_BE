package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalHistory;
import com.ohgiraffer.approval.domain.repository.ApprovalHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ApprovalHistoryRepositoryAdapter
        implements ApprovalHistoryRepository {

    private final SpringDataApprovalHistoryRepository repository;

    public ApprovalHistoryRepositoryAdapter(
            SpringDataApprovalHistoryRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ApprovalHistory save(
            ApprovalHistory approvalHistory
    ) {
        ApprovalHistoryJpaEntity entity =
                ApprovalHistoryJpaEntity.from(
                        approvalHistory
                );

        ApprovalHistoryJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public List<ApprovalHistory> findAllByApprovalIdOrderByChangedAtAsc(
            Long approvalId
    ) {
        return repository.findAllByApprovalIdOrderByChangedAtAsc(
                        approvalId
                )
                .stream()
                .map(
                        ApprovalHistoryJpaEntity::toDomain
                )
                .toList();
    }
}