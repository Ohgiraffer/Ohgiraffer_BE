package com.ohgiraffer.submission.domain.model;

public record SubmissionListEntry(
        Long submissionId,
        Long submissionBoxId,
        Long ownerUserId,
        Long teamId
) {
}