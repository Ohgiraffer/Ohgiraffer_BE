package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.submission.domain.model.Submission;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateSubmissionResult(
        Long submissionId,
        Long submissionBoxId,
        Long ownerUserId,
        Long teamId,
        Long submittedBy,
        LocalDateTime submittedAt,
        boolean late,
        List<UpdateSubmissionItemResult> items
) {

    public UpdateSubmissionResult {
        items = List.copyOf(items);
    }

    public static UpdateSubmissionResult from(
            Submission submission
    ) {
        List<UpdateSubmissionItemResult> itemResults =
                submission.getItemValues()
                        .stream()
                        .map(UpdateSubmissionItemResult::from)
                        .toList();

        return new UpdateSubmissionResult(
                submission.getId(),
                submission.getSubmissionBoxId(),
                submission.getOwnerUserId(),
                submission.getTeamId(),
                submission.getSubmittedBy(),
                submission.getSubmittedAt(),
                submission.isLate(),
                itemResults
        );
    }
}