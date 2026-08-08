package com.ohgiraffer.submissionbox.presentation.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxListResult;
import com.ohgiraffer.submissionbox.application.usecase.SubmissionBoxStatus;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubmissionBoxListResponse(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int itemCount,
        SubmissionBoxStatus status,
        boolean acceptingSubmissions,
        boolean lateSubmission,
        Integer submittedCount,
        Integer targetCount,
        Boolean submitted,
        Long submissionId
) {

    public static SubmissionBoxListResponse from(
            SubmissionBoxListResult result
    ) {
        return new SubmissionBoxListResponse(
                result.submissionBoxId(),
                result.projectName(),
                result.targetScope(),
                result.startAt(),
                result.dueAt(),
                result.latePolicy(),
                result.itemCount(),
                result.status(),
                result.acceptingSubmissions(),
                result.lateSubmission(),
                result.submittedCount(),
                result.targetCount(),
                result.submitted(),
                result.submissionId()
        );
    }
}