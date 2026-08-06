package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submission.domain.model.Submission;

import java.time.LocalDateTime;

public record CreateSubmissionResult(
        Long submissionId,
        Long submissionBoxId,
        Long ownerUserId,
        Long teamId,
        Long submittedBy,
        LocalDateTime submittedAt,
        boolean late
) {

    public static CreateSubmissionResult from(
            Submission submission
    ) {
        return new CreateSubmissionResult(
                submission.getId(),
                submission.getSubmissionBoxId(),
                submission.getOwnerUserId(),
                submission.getTeamId(),
                submission.getSubmittedBy(),
                submission.getSubmittedAt(),
                submission.isLate()
        );
    }
}