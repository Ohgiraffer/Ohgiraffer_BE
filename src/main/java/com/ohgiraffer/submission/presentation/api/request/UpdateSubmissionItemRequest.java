package com.ohgiraffer.submission.presentation.api.request;

import com.ohgiraffer.submission.application.command.UpdateSubmissionItemCommand;
import jakarta.validation.constraints.NotNull;

public record UpdateSubmissionItemRequest(

        @NotNull
        Long submissionBoxItemId,

        Integer fileIndex,

        String externalUrl
) {

    public UpdateSubmissionItemCommand toCommand() {
        return new UpdateSubmissionItemCommand(
                submissionBoxItemId,
                fileIndex,
                externalUrl
        );
    }
}