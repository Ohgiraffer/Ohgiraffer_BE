package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface PreviewSubmissionFileUseCase {

    PreviewSubmissionFileResult createPreview(
            Long submissionItemValueId,
            Long requesterId,
            Role requesterRole
    );
}