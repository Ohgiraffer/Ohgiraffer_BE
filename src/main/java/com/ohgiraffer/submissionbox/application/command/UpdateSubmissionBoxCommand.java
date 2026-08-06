package com.ohgiraffer.submissionbox.application.command;

import com.ohgiraffer.submissionbox.domain.model.LatePolicy;
import com.ohgiraffer.submissionbox.domain.model.SubmissionTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateSubmissionBoxCommand(
        Long submissionBoxId,
        String projectName,
        SubmissionTargetScope targetScope,
        LocalDateTime startAt,
        LocalDateTime dueAt,
        LatePolicy latePolicy,
        List<UpdateSubmissionBoxItemCommand> items
) {

    public UpdateSubmissionBoxCommand {
        if (items != null) {
            items = List.copyOf(items);
        }
    }
}