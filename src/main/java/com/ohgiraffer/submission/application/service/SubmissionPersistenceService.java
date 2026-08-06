package com.ohgiraffer.submission.application.service;

import com.ohgiraffer.submission.domain.model.Submission;
import com.ohgiraffer.submission.domain.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionPersistenceService {

    private final SubmissionRepository submissionRepository;

    @Transactional
    public Submission save(
            Submission submission
    ) {
        return submissionRepository.save(submission);
    }
}