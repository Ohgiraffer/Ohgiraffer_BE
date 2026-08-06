package com.ohgiraffer.submission.domain.repository;

import com.ohgiraffer.submission.domain.model.SubmissionItemValue;

import java.util.Optional;

public interface SubmissionItemValueRepository {

    Optional<SubmissionItemValue> findById(
            Long submissionItemValueId
    );
}