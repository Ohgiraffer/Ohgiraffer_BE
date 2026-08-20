package com.ohgiraffer.approval.infrastructure.adapter;

import com.ohgiraffer.approval.domain.model.profile.ApprovalApplicantProfile;
import com.ohgiraffer.approval.domain.repository.ApprovalApplicantProfileRepository;
import com.ohgiraffer.approval.infrastructure.persistence.ApprovalApplicantProfileJpaEntity;
import com.ohgiraffer.approval.infrastructure.persistence.SpringDataApprovalApplicantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApprovalApplicantProfileRepositoryAdapter
        implements ApprovalApplicantProfileRepository {

    private final SpringDataApprovalApplicantProfileRepository repository;

    @Override
    public ApprovalApplicantProfile save(
            ApprovalApplicantProfile approvalApplicantProfile
    ) {
        ApprovalApplicantProfileJpaEntity entity =
                ApprovalApplicantProfileJpaEntity.from(
                        approvalApplicantProfile
                );

        ApprovalApplicantProfileJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<ApprovalApplicantProfile> findByUserId(
            Long userId
    ) {
        return repository.findByUserId(
                        userId
                )
                .map(
                        ApprovalApplicantProfileJpaEntity::toDomain
                );
    }
}