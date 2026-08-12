package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

public record StudentSubmissionHistoryItemResult(

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
}