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
        int submittedCount,
        Integer targetCount,
        Long mySubmissionId,
        boolean canSubmit,
        boolean canEdit,
        List<SubmissionBoxItemResponse> items,
        List<SubmissionStatusResponse> submissions
) {

    public SubmissionBoxDetailResponse {
        items = List.copyOf(items);
        submissions = List.copyOf(submissions);
    }

    public static SubmissionBoxDetailResponse from(
            SubmissionBoxDetailResult result
    ) {
        List<SubmissionBoxItemResponse> itemResponses =
                result.items()
                        .stream()
                        .map(SubmissionBoxItemResponse::from)
                        .toList();

        List<SubmissionStatusResponse> submissionResponses =
                result.submissions()
                        .stream()
                        .map(SubmissionStatusResponse::from)
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
                result.submittedCount(),
                result.targetCount(),
                result.mySubmissionId(),
                result.canSubmit(),
                result.canEdit(),
                itemResponses,
                submissionResponses
        );
    }
}