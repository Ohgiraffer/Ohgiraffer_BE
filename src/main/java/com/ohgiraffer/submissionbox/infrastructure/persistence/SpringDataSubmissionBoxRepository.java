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
    @EntityGraph(attributePaths = "items")
    @Query("""
        SELECT submissionBox
        FROM SubmissionBoxJpaEntity submissionBox
        WHERE submissionBox.id = :submissionBoxId
        """)
    Optional<SubmissionBoxJpaEntity> findWithItemsByIdForUpdate(
            @Param("submissionBoxId") Long submissionBoxId
    );

    @Query(
            value = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM submission
                        WHERE submission_box_id = :submissionBoxId
                    )
                    """,
            nativeQuery = true
    )
    boolean existsSubmissionBySubmissionBoxId(
            @Param("submissionBoxId")
            Long submissionBoxId
    );
}