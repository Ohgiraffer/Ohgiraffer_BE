package com.ohgiraffer.submissionbox.infrastructure.persistence;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.repository.SubmissionBoxRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;

@Repository
public class SubmissionBoxRepositoryAdapter implements SubmissionBoxRepository {

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
    public SubmissionBox update(
            SubmissionBox submissionBox
    ) {
        SubmissionBoxJpaEntity entity =
                repository.findWithItemsById(
                        submissionBox.getId()
                ).orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.SUBMISSION_BOX_NOT_FOUND
                        )
                );

        entity.updateFrom(submissionBox);

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

    @Override
    public Optional<SubmissionBox> findByIdForUpdate(
            Long submissionBoxId
    ) {
        return repository
                .findWithItemsByIdForUpdate(submissionBoxId)
                .map(SubmissionBoxJpaEntity::toDomain);
    }

    @Override
    public boolean existsById(
            Long submissionBoxId
    ) {
        return repository.existsById(submissionBoxId);
    }

    @Override
    public boolean hasSubmissions(
            Long submissionBoxId
    ) {
        return repository
                .existsSubmissionBySubmissionBoxId(
                        submissionBoxId
                );
    }

    @Override
    public void deleteById(
            Long submissionBoxId
    ) {
        repository.deleteById(submissionBoxId);
        repository.flush();
    }
}