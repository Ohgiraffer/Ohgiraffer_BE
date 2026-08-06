package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface SpringDataSubmissionRepository
        extends JpaRepository<SubmissionJpaEntity, Long> {

    boolean existsBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    boolean existsBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    Optional<SubmissionJpaEntity>
    findBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    Optional<SubmissionJpaEntity>
    findBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    @EntityGraph(attributePaths = "itemValues")
    List<SubmissionJpaEntity>
    findAllBySubmissionBoxIdOrderBySubmittedAtAsc(
            Long submissionBoxId
    );

}