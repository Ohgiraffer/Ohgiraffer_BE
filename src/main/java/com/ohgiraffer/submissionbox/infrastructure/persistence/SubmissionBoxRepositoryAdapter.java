package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionBoxRepositoryAdapter
        implements SubmissionBoxRepository {

    private final SpringDataSubmissionBoxRepository repository;

    public SubmissionBoxRepositoryAdapter(
            SpringDataSubmissionBoxRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public SubmissionBox save(
            SubmissionBox submissionBox
    ) {
        SubmissionBoxJpaEntity entity =
                SubmissionBoxJpaEntity.from(submissionBox);

        SubmissionBoxJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return savedEntity.toDomain();
    }

    @Override
    public List<SubmissionBox> findAll() {
        return repository.findAllByOrderByDueAtDesc()
                .stream()
                .map(SubmissionBoxJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<SubmissionBox> findById(
            Long submissionBoxId
    ) {
        return repository.findWithItemsById(submissionBoxId)
                .map(SubmissionBoxJpaEntity::toDomain);
    }
}