package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.budget.ExternalSheetLink;
import com.ohgiraffer.approval.domain.repository.ExternalSheetLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ExternalSheetLinkRepositoryAdapter
        implements ExternalSheetLinkRepository {

    private final SpringDataExternalSheetLinkRepository repository;

    public ExternalSheetLinkRepositoryAdapter(
            SpringDataExternalSheetLinkRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public ExternalSheetLink save(
            ExternalSheetLink externalSheetLink
    ) {
        ExternalSheetLinkJpaEntity entity =
                ExternalSheetLinkJpaEntity.from(
                        externalSheetLink
                );

        ExternalSheetLinkJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<ExternalSheetLink> findByDomain(
            String domain
    ) {
        return repository
                .findByDomain(
                        domain
                )
                .map(
                        ExternalSheetLinkJpaEntity::toDomain
                );
    }
}