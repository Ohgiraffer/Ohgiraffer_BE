package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submission.domain.model.Submission;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionStatusResult(
        Long targetId,
        String targetName,
        Long submissionId,
        boolean submitted,
        boolean mine,
        boolean late,
        LocalDateTime submittedAt,
        boolean canSubmit,
        boolean canEdit,
        List<SubmissionValueResult> values
) {

    public SubmissionStatusResult {
        values = List.copyOf(values);
    }

    public static SubmissionStatusResult submitted(
            Submission submission,
            String targetName,
            boolean mine,
            boolean editable
    ) {
        Long targetId =
                resolveTargetId(submission);

        List<SubmissionValueResult> valueResults =
                submission.getItemValues()
                        .stream()
                        .map(SubmissionValueResult::from)
                        .toList();

        return new SubmissionStatusResult(
                targetId,
                targetName,
                submission.getId(),
                true,
                mine,
                submission.isLate(),
                submission.getSubmittedAt(),
                false,
                mine && editable,
                valueResults
        );
    }

    public static SubmissionStatusResult notSubmitted(
            Long targetId,
            String targetName,
            boolean mine,
            boolean acceptingSubmissions
    ) {
        return new SubmissionStatusResult(
                targetId,
                targetName,
                null,
                false,
                mine,
                false,
                null,
                mine && acceptingSubmissions,
                false,
                List.of()
        );
    }

    private static Long resolveTargetId(
            Submission submission
    ) {
        if (submission.getTeamId() != null) {
            return submission.getTeamId();
        }

        return submission.getOwnerUserId();
    }
}