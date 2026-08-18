package com.ohgiraffer.submissionbox.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT submissionBox
    FROM SubmissionBoxJpaEntity submissionBox
    WHERE submissionBox.id = :submissionBoxId
    """)
    Optional<SubmissionBoxJpaEntity> findByIdForUpdate(
            @Param("submissionBoxId") Long submissionBoxId
    );

    @Query(
            value = """
                SELECT COUNT(*)
                FROM submission
                WHERE submission_box_id = :submissionBoxId
                """,
            nativeQuery = true
    )
    long countSubmissionsBySubmissionBoxId(
            @Param("submissionBoxId") Long submissionBoxId
    );

    @EntityGraph(attributePaths = "items")
    @Query("""
        SELECT submissionBox
        FROM SubmissionBoxJpaEntity submissionBox
        JOIN UserJpaEntity creator
          ON creator.id = submissionBox.createdBy
        WHERE creator.bootcampId = :bootcampId
        ORDER BY submissionBox.dueAt DESC
        """)
    List<SubmissionBoxJpaEntity> findAllByBootcampIdOrderByDueAtDesc(
            @Param("bootcampId") Long bootcampId
    );
}