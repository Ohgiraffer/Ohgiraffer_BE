package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.application.command.CreateSubmissionBoxCommand;

public interface CreateSubmissionBoxUseCase {

    CreateSubmissionBoxResult create(
            CreateSubmissionBoxCommand command
    );
}