package com.ohgiraffer.submissionbox.domain.repository;

import com.ohgiraffer.submissionbox.domain.model.SubmissionBox;

import java.util.List;
import java.util.Optional;

public interface SubmissionBoxRepository {

    SubmissionBox save(
            SubmissionBox submissionBox
    );

    SubmissionBox update(
            SubmissionBox submissionBox
    );

    List<SubmissionBox> findAll();

    Optional<SubmissionBox> findById(
            Long submissionBoxId
    );

    Optional<SubmissionBox> findByIdForUpdate(
            Long submissionBoxId
    );

    boolean existsById(
            Long submissionBoxId
    );

    boolean hasSubmissions(
            Long submissionBoxId
    );

    void deleteById(
            Long submissionBoxId
    );
}