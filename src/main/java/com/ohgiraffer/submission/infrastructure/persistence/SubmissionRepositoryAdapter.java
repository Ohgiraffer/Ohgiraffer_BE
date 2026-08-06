package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubmissionRepositoryAdapter
        implements SubmissionRepository {

    private final SpringDataSubmissionRepository repository;

    @Override
    public Submission save(Submission submission) {
        SubmissionJpaEntity entity =
                SubmissionJpaEntity.from(submission);

        SubmissionJpaEntity savedEntity =
                repository.saveAndFlush(entity);

        return savedEntity.toDomain();
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
}