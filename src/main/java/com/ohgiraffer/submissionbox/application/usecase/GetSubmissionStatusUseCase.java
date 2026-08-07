package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetSubmissionStatusUseCase {

    SubmissionStatusDetailResult getSubmissionStatus(
            Long submissionBoxId,
            Long requesterId,
            Role requesterRole,
            String keyword,
            String status,
            int page,
            int size
    );
}