package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubmissionRepositoryAdapter
        implements SubmissionRepository {

    private final SpringDataSubmissionRepository repository;

    @Override
    public Submission save(Submission submission) {
        SubmissionJpaEntity entity =
                SubmissionJpaEntity.from(submission);

        try {
            SubmissionJpaEntity savedEntity =
                    repository.saveAndFlush(entity);

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateSubmissionViolation(exception)) {
                throw new BusinessException(
                        ErrorCode.SUBMISSION_ALREADY_EXISTS
                );
            }

            throw exception;
        }
    }

    private boolean isDuplicateSubmissionViolation(
            DataIntegrityViolationException exception
    ) {
        Throwable current = exception;

        while (current != null) {
            String message = current.getMessage();

            if (message != null
                    && (message.contains(
                    "UQ_SUBMISSION_BOX_OWNER_USER"
            )
                    || message.contains(
                    "UQ_SUBMISSION_BOX_TEAM"
            ))) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    @Override
    public boolean existsBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    ) {
        return repository
                .existsBySubmissionBoxIdAndOwnerUserId(
                        submissionBoxId,
                        ownerUserId
                );
    }

    @Override
    public boolean existsBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    ) {
        return repository
                .existsBySubmissionBoxIdAndTeamId(
                        submissionBoxId,
                        teamId
                );
    }

    @Override
    public Optional<Submission>
    findBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    ) {
        return repository
                .findBySubmissionBoxIdAndOwnerUserId(
                        submissionBoxId,
                        ownerUserId
                )
                .map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public Optional<Submission>
    findBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    ) {
        return repository
                .findBySubmissionBoxIdAndTeamId(
                        submissionBoxId,
                        teamId
                )
                .map(SubmissionJpaEntity::toDomain);
    }

    @Override
    public List<Submission> findAllBySubmissionBoxId(
            Long submissionBoxId
    ) {
        return repository
                .findAllBySubmissionBoxIdOrderBySubmittedAtAsc(
                        submissionBoxId
                )
                .stream()
                .map(SubmissionJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Submission> findById(
            Long submissionId
    ) {
        return repository
                .findDetailById(submissionId)
                .map(SubmissionJpaEntity::toDomain);
    }

}