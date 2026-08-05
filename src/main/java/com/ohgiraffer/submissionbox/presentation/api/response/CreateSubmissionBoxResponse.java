package com.ohgiraffer.submissionbox.presentation.api.response;

import com.ohgiraffer.submissionbox.application.usecase.CreateSubmissionBoxResult;
import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

public record CreateSubmissionBoxResponse(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int itemCount
) {

    public static CreateSubmissionBoxResponse from(
            CreateSubmissionBoxResult result
    ) {
        return new CreateSubmissionBoxResponse(
                result.submissionBoxId(),
                result.projectName(),
                result.targetScope(),
                result.startAt(),
                result.dueAt(),
                result.latePolicy(),
                result.itemCount()
        );
    }
}