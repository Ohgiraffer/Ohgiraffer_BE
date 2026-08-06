package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.approval.ApprovalPurchaseDetail;
import com.ohgiraffer.approval.domain.repository.ApprovalPurchaseDetailRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ApprovalPurchaseDetailRepositoryAdapter
        implements ApprovalPurchaseDetailRepository {

    private final SpringDataApprovalPurchaseDetailRepository repository;

    public ApprovalPurchaseDetailRepositoryAdapter(
            SpringDataApprovalPurchaseDetailRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ApprovalPurchaseDetail save(
            ApprovalPurchaseDetail approvalPurchaseDetail
    ) {
        ApprovalPurchaseDetailJpaEntity entity =
                ApprovalPurchaseDetailJpaEntity.from(
                        approvalPurchaseDetail
                );

        ApprovalPurchaseDetailJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<ApprovalPurchaseDetail> findByApprovalId(
            Long approvalId
    ) {
        return repository
                .findByApprovalId(
                        approvalId
                )
                .map(
                        ApprovalPurchaseDetailJpaEntity::toDomain
                );
    }
}