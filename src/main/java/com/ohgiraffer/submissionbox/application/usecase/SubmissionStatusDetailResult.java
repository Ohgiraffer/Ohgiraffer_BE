package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionStatusDetailResult(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int submittedCount,
        Integer targetCount,
        List<SubmissionBoxItemResult> items,
        int page,
        int size,
        long filteredCount,
        int totalPages,
        List<SubmissionStatusResult> submissions
) {

    public SubmissionStatusDetailResult {
        items = items == null
                ? List.of()
                : List.copyOf(items);

        submissions = submissions == null
                ? List.of()
                : List.copyOf(submissions);
    }
}