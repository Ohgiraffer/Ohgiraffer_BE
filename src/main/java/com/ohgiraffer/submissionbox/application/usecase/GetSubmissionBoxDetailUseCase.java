package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetSubmissionBoxDetailUseCase {

    SubmissionBoxDetailResult getSubmissionBox(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole
    );
}