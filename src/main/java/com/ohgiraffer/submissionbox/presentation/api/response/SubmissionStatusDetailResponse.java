package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.SubmissionStatusDetailResult;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionStatusDetailResponse(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int submittedCount,
        Integer targetCount,
        List<SubmissionBoxItemResponse> items,
        int page,
        int size,
        long filteredCount,
        int totalPages,
        List<SubmissionStatusResponse> submissions
) {

    public SubmissionStatusDetailResponse {
        items = items == null
                ? List.of()
                : List.copyOf(items);

        submissions = submissions == null
                ? List.of()
                : List.copyOf(submissions);
    }

    public static SubmissionStatusDetailResponse from(
            SubmissionStatusDetailResult result
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

        return new SubmissionStatusDetailResponse(
                result.submissionBoxId(),
                result.projectName(),
                result.targetScope(),
                result.startAt(),
                result.dueAt(),
                result.latePolicy(),
                result.submittedCount(),
                result.targetCount(),
                itemResponses,
                result.page(),
                result.size(),
                result.filteredCount(),
                result.totalPages(),
                submissionResponses
        );
    }
}