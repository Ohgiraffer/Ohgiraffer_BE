package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusResult;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionStatusResponse(
        Long targetId,
        String targetName,
        Long submissionId,
        boolean submitted,
        boolean mine,
        boolean late,
        LocalDateTime submittedAt,
        boolean canSubmit,
        boolean canEdit,
        List<SubmissionValueResponse> values
) {

    public SubmissionStatusResponse {
        values = List.copyOf(values);
    }

    public static SubmissionStatusResponse from(
            SubmissionStatusResult result
    ) {
        List<SubmissionValueResponse> valueResponses =
                result.values()
                        .stream()
                        .map(SubmissionValueResponse::from)
                        .toList();

        return new SubmissionStatusResponse(
                result.targetId(),
                result.targetName(),
                result.submissionId(),
                result.submitted(),
                result.mine(),
                result.late(),
                result.submittedAt(),
                result.canSubmit(),
                result.canEdit(),
                valueResponses
        );
    }
}