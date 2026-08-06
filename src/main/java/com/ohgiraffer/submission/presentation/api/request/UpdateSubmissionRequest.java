package com.ohgiraffer.submission.presentation.api.request;

import com.ohgiraffer.submission.application.command.UpdateSubmissionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateSubmissionRequest(

        @NotEmpty
        List<
                @NotNull
                @Valid
                        UpdateSubmissionItemRequest
                > items
) {

    public UpdateSubmissionCommand toCommand(
            Long submissionId,
            Long requestedBy
    ) {
        return new UpdateSubmissionCommand(
                submissionId,
                requestedBy,
                items.stream()
                        .map(
                                UpdateSubmissionItemRequest
                                        ::toCommand
                        )
                        .toList()
        );
    }
}