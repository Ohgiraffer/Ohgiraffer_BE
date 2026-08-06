package com.ohgiraffer.submission.presentation.api.request;

import com.ohgiraffer.submission.application.command.CreateSubmissionItemCommand;
import jakarta.validation.constraints.NotNull;

public record CreateSubmissionItemRequest(

        @NotNull
        Long submissionBoxItemId,

        Integer fileIndex,

        String externalUrl
) {

    public CreateSubmissionItemCommand toCommand() {
        return new CreateSubmissionItemCommand(
                submissionBoxItemId,
                fileIndex,
                externalUrl
        );
    }
}