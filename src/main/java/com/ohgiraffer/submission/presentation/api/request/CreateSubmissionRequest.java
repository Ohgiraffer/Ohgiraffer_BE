package com.ohgiraffer.submission.presentation.api.request;

import com.ohgiraffer.submission.application.command.CreateSubmissionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateSubmissionRequest(

        @NotNull
        @Positive
        Long submissionBoxId,

        @NotEmpty
        List<
                @NotNull
                @Valid
                        CreateSubmissionItemRequest
                > items
) {

    public CreateSubmissionCommand toCommand(
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