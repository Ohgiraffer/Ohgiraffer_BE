package com.ohgiraffer.submission.domain.repository;

import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.model.SubmissionListEntry;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository {

    Submission save(Submission submission);

    boolean existsBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    boolean existsBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    Optional<Submission> findBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    Optional<Submission> findBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    List<Submission> findAllBySubmissionBoxId(
            Long submissionBoxId
    );

    List<SubmissionListEntry>
    findListEntriesBySubmissionBoxIds(
            Collection<Long> submissionBoxIds
    );

    Optional<Submission> findById(
            Long submissionId
    );
}