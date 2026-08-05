package com.ohgiraffer.submissionbox.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataSubmissionBoxRepository
        extends JpaRepository<SubmissionBoxJpaEntity, Long> {

    @EntityGraph(attributePaths = "items")
    List<SubmissionBoxJpaEntity> findAllByOrderByDueAtDesc();

    @EntityGraph(attributePaths = "items")
    Optional<SubmissionBoxJpaEntity> findWithItemsById(
            Long id
    );
}