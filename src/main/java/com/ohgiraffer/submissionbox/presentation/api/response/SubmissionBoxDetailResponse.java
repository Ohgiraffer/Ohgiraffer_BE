package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxDetailResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxStatus;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionBoxDetailResponse(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        Long createdBy,
        SubmissionBoxStatus status,
        boolean acceptingSubmissions,
        boolean lateSubmission,
        List<SubmissionBoxItemResponse> items
) {

    public SubmissionBoxDetailResponse {
        items = List.copyOf(items);
    }

    public static SubmissionBoxDetailResponse from(
            SubmissionBoxDetailResult result
    ) {
        List<SubmissionBoxItemResponse> itemResponses =
                result.items()
                        .stream()
                        .map(SubmissionBoxItemResponse::from)
                        .toList();

        return new SubmissionBoxDetailResponse(
                result.submissionBoxId(),
                result.projectName(),
                result.targetScope(),
                result.startAt(),
                result.dueAt(),
                result.latePolicy(),
                result.createdBy(),
                result.status(),
                result.acceptingSubmissions(),
                result.lateSubmission(),
                itemResponses
        );
    }
}