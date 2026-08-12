package com.ohgiraffer.submission.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetStudentSubmissionHistoryUseCase {

    StudentSubmissionHistoryResult getHistory(
            Long studentId,
            Long requesterId,
            Role requesterRole
    );
}