package com.ohgiraffer.submission.domain.repository;

import com.ohgiraffer.submission.domain.model.Submission;

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
}