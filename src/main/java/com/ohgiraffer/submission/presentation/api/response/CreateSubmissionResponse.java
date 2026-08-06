package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.CreateSubmissionResult;

import java.time.LocalDateTime;

public record CreateSubmissionResponse(
        Long submissionId,
        Long submissionBoxId,
        Long ownerUserId,
        Long teamId,
        Long submittedBy,
        LocalDateTime submittedAt,
        boolean late
) {

    public static CreateSubmissionResponse from(
            CreateSubmissionResult result
    ) {
        return new CreateSubmissionResponse(
                result.submissionId(),
                result.submissionBoxId(),
                result.ownerUserId(),
                result.teamId(),
                result.submittedBy(),
                result.submittedAt(),
                result.late()
        );
    }
}