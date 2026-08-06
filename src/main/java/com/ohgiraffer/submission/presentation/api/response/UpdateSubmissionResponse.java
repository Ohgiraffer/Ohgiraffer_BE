package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.UpdateSubmissionResult;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateSubmissionResponse(
        Long submissionId,
        Long submissionBoxId,
        Long ownerUserId,
        Long teamId,
        Long submittedBy,
        LocalDateTime submittedAt,
        boolean late,
        List<UpdateSubmissionItemResponse> items
) {

    public UpdateSubmissionResponse {
        items = List.copyOf(items);
    }

    public static UpdateSubmissionResponse from(
            UpdateSubmissionResult result
    ) {
        List<UpdateSubmissionItemResponse> itemResponses =
                result.items()
                        .stream()
                        .map(UpdateSubmissionItemResponse::from)
                        .toList();

        return new UpdateSubmissionResponse(
                result.submissionId(),
                result.submissionBoxId(),
                result.ownerUserId(),
                result.teamId(),
                result.submittedBy(),
                result.submittedAt(),
                result.late(),
                itemResponses
        );
    }
}