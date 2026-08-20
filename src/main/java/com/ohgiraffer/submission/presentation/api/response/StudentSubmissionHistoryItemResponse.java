package com.ohgiraffer.submission.presentation.api.response;

import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryItemResult;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistorySourceType;
import com.ohgiraffer.submission.application.usecase.StudentSubmissionHistoryStatus;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

public record StudentSubmissionHistoryItemResponse(

        StudentSubmissionHistorySourceType sourceType,

        Long targetId,

        String title,

        SubmissionTargetScope targetScope,

        StudentSubmissionHistoryStatus status,

        boolean completed,

        LocalDateTime completedAt,

        LocalDateTime dueAt,

        boolean late
) {

    public static StudentSubmissionHistoryItemResponse from(
            StudentSubmissionHistoryItemResult result
    ) {
        return new StudentSubmissionHistoryItemResponse(
                result.sourceType(),
                result.targetId(),
                result.title(),
                result.targetScope(),
                result.status(),
                result.completed(),
                result.completedAt(),
                result.dueAt(),
                result.late()
        );
    }
}