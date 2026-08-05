package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.application.command.UpdateSubmissionBoxCommand;

public interface UpdateSubmissionBoxUseCase {

    SubmissionBoxDetailResult update(
            UpdateSubmissionBoxCommand command
    );
}