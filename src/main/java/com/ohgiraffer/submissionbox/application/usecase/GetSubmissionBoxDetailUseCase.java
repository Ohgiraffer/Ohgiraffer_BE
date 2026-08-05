package com.ohgiraffer.submissionbox.application.usecase;

public interface GetSubmissionBoxDetailUseCase {

    SubmissionBoxDetailResult getSubmissionBox(
            Long submissionBoxId
    );
}