package com.ohgiraffer.submission.presentation.api.request;

import com.ohgiraffer.submission.application.command.CreateSubmissionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateSubmissionRequest(

        @NotEmpty
        List<
                @NotNull
                @Valid
                        CreateSubmissionItemRequest
                > items
) {

    public CreateSubmissionCommand toCommand(
            Long submissionBoxId,
            Long submittedBy
    ) {
        return new CreateSubmissionCommand(
                submissionBoxId,
                submittedBy,
                items.stream()
                        .map(
                                CreateSubmissionItemRequest
                                        ::toCommand
                        )
                        .toList()
        );
    }
}