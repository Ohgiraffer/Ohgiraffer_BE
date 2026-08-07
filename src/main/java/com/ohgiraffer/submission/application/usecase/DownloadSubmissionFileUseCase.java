package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface DownloadSubmissionFileUseCase {

    DownloadSubmissionFileResult createDownload(
            Long submissionItemValueId,
            Long requesterId,
            Role requesterRole
    );
}