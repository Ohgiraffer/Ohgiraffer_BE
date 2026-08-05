package com.ohgiraffer.submissionbox.application.usecase;

import java.util.List;

public interface GetSubmissionBoxListUseCase {

    List<SubmissionBoxListResult> getSubmissionBoxes();
}