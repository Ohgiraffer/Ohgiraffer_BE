package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.submissionbox.application.command.UpdateSubmissionBoxCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface UpdateSubmissionBoxUseCase {

    SubmissionBoxDetailResult update(
            UpdateSubmissionBoxCommand command,
            Long requesterId,
            Role requesterRole
    );
}