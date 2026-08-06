package com.ohgiraffer.submission.application.command;

import java.util.List;

public record CreateSubmissionCommand(
        Long submissionBoxId,
        Long submittedBy,
        List<CreateSubmissionItemCommand> items
) {
}