package com.ohgiraffer.submission.infrastructure.persistence;

import com.ohgiraffer.submission.domain.model.SubmissionItemValue;
import com.ohgiraffer.submission.domain.repository.SubmissionItemValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubmissionItemValueRepositoryAdapter
        implements SubmissionItemValueRepository {

    private final SpringDataSubmissionItemValueRepository
            repository;

    @Override
    public Optional<SubmissionItemValue> findById(
            Long submissionItemValueId
    ) {
        return repository
                .findDetailById(
                        submissionItemValueId
                )
                .map(
                        SubmissionItemValueJpaEntity
                                ::toDomain
                );
    }
}