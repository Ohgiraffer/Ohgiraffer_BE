package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataSubmissionItemValueRepository
        extends JpaRepository<
        SubmissionItemValueJpaEntity,
        Long
        > {

    @Query("""
            SELECT value
            FROM SubmissionItemValueJpaEntity value
            JOIN FETCH value.submission
            WHERE value.id = :submissionItemValueId
            """)
    Optional<SubmissionItemValueJpaEntity>
    findDetailById(
            @Param("submissionItemValueId")
            Long submissionItemValueId
    );
}