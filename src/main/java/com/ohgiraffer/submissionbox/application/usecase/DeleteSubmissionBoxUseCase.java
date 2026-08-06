package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface DeleteSubmissionBoxUseCase {

    void delete(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    );
}