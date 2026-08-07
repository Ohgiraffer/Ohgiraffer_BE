package com.ohgiraffer.submission.application.command;

public record UpdateSubmissionItemCommand(
        Long submissionBoxItemId,
        Integer fileIndex,
        String externalUrl
) {
}