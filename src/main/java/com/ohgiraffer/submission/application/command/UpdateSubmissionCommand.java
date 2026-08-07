package com.ohgiraffer.submission.application.command;

import java.util.List;

public record UpdateSubmissionCommand(
        Long submissionId,
        Long requestedBy,
        List<UpdateSubmissionItemCommand> items
) {
}