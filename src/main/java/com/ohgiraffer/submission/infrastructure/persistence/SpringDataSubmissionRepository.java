package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

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
}