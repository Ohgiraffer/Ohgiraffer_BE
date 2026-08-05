package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;

public record SubmissionBoxListResult(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        int itemCount,
        SubmissionBoxStatus status,
        boolean acceptingSubmissions,
        boolean lateSubmission
) {

    public static SubmissionBoxListResult from(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        SubmissionBoxStatus status =
                calculateStatus(submissionBox, now);

        boolean lateSubmission =
                now.isAfter(submissionBox.getDueAt());

        boolean acceptingSubmissions =
                canSubmit(submissionBox, now);

        return new SubmissionBoxListResult(
                submissionBox.getId(),
                submissionBox.getProjectName(),
                submissionBox.getTargetScope(),
                submissionBox.getStartAt(),
                submissionBox.getDueAt(),
                submissionBox.getLatePolicy(),
                submissionBox.getItems().size(),
                status,
                acceptingSubmissions,
                lateSubmission
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