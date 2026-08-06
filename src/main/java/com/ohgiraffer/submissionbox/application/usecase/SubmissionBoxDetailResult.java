package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionBoxDetailResult(
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
        List<SubmissionBoxItemResult> items
) {

    public SubmissionBoxDetailResult {
        items = List.copyOf(items);
    }

    public static SubmissionBoxDetailResult from(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        SubmissionBoxStatus status =
                calculateStatus(submissionBox, now);

        boolean lateSubmission =
                now.isAfter(submissionBox.getDueAt());

        boolean acceptingSubmissions =
                canSubmit(submissionBox, now);

        List<SubmissionBoxItemResult> itemResults =
                submissionBox.getItems()
                        .stream()
                        .map(SubmissionBoxItemResult::from)
                        .toList();

        return new SubmissionBoxDetailResult(
                submissionBox.getId(),
                submissionBox.getProjectName(),
                submissionBox.getTargetScope(),
                submissionBox.getStartAt(),
                submissionBox.getDueAt(),
                submissionBox.getLatePolicy(),
                submissionBox.getCreatedBy(),
                status,
                acceptingSubmissions,
                lateSubmission,
                itemResults
        );
    }

    private static SubmissionBoxStatus calculateStatus(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            return SubmissionBoxStatus.UPCOMING;
        }

        if (now.isAfter(submissionBox.getDueAt())) {
            return SubmissionBoxStatus.CLOSED;
        }

        return SubmissionBoxStatus.OPEN;
    }

    private static boolean canSubmit(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            return false;
        }

        if (!now.isAfter(submissionBox.getDueAt())) {
            return true;
        }

        return submissionBox.getLatePolicy()
                == LatePolicy.ALLOW;
    }
}