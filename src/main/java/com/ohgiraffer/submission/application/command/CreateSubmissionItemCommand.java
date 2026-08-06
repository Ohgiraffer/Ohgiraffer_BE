package com.ohgiraffer.submission.application.command;

public record CreateSubmissionItemCommand(
        Long submissionBoxItemId,
        Integer fileIndex,
        String externalUrl
) {
}