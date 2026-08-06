package com.ohgiraffer.submissionbox.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetSubmissionBoxListUseCase {

    List<SubmissionBoxListResult> getSubmissionBoxes(
            Long userId,
            Role role
    );
}