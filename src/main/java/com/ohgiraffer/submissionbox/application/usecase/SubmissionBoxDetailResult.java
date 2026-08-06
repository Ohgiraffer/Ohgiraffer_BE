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
        int submittedCount,
        Integer targetCount,
        Long mySubmissionId,
        boolean canSubmit,
        boolean canEdit,
        List<SubmissionBoxItemResult> items,
        List<SubmissionStatusResult> submissions
) {

    public SubmissionBoxDetailResult {
        items = List.copyOf(items);
        submissions = List.copyOf(submissions);
    }

    public static SubmissionBoxDetailResult from(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        return from(
                submissionBox,
                now,
                null,
                null,
                false,
                List.of()
        );
    }

    public static SubmissionBoxDetailResult from(
            SubmissionBox submissionBox,
            LocalDateTime now,
            Integer targetCount,
            Long mySubmissionId,
            boolean eligibleToSubmit,
            List<SubmissionStatusResult> submissions
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

        int submittedCount =
                (int) submissions.stream()
                        .filter(SubmissionStatusResult::submitted)
                        .count();

        boolean canSubmit =
                eligibleToSubmit
                        && acceptingSubmissions
                        && mySubmissionId == null;

        boolean canEdit =
                eligibleToSubmit
                        && isEditable(submissionBox, now)
                        && mySubmissionId != null;

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
                submittedCount,
                targetCount,
                mySubmissionId,
                canSubmit,
                canEdit,
                itemResults,
                submissions
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

    private static boolean isEditable(
            SubmissionBox submissionBox,
            LocalDateTime now
    ) {
        if (now.isBefore(submissionBox.getStartAt())) {
            return false;
        }

        return !now.isAfter(
                submissionBox.getDueAt()
        );
    }
}