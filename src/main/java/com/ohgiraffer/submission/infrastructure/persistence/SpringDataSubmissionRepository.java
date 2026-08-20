package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataSubmissionRepository
        extends JpaRepository<SubmissionJpaEntity, Long> {

    boolean existsBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    boolean existsBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    @EntityGraph(attributePaths = "itemValues")
    Optional<SubmissionJpaEntity>
    findBySubmissionBoxIdAndOwnerUserId(
            Long submissionBoxId,
            Long ownerUserId
    );

    @EntityGraph(attributePaths = "itemValues")
    Optional<SubmissionJpaEntity>
    findBySubmissionBoxIdAndTeamId(
            Long submissionBoxId,
            Long teamId
    );

    @EntityGraph(attributePaths = "itemValues")
    List<SubmissionJpaEntity>
    findAllBySubmissionBoxIdOrderBySubmittedAtAsc(
            Long submissionBoxId
    );

    @Query("""
        SELECT new com.ohgiraffer.submission.domain.model.SubmissionListEntry(
            submission.id,
            submission.submissionBoxId,
            submission.ownerUserId,
            submission.teamId
        )
        FROM SubmissionJpaEntity submission
        WHERE submission.submissionBoxId
            IN :submissionBoxIds
        """)
    List<com.ohgiraffer.submission.domain.model.SubmissionListEntry>
    findListEntriesBySubmissionBoxIds(
            @Param("submissionBoxIds")
            Collection<Long> submissionBoxIds
    );

    @EntityGraph(attributePaths = "itemValues")
    @Query("""
        SELECT submission
        FROM SubmissionJpaEntity submission
        WHERE submission.id = :submissionId
        """)
    Optional<SubmissionJpaEntity> findDetailById(
            @Param("submissionId")
            Long submissionId
    );

}