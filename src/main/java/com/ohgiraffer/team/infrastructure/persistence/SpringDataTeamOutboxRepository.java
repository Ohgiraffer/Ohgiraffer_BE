package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamOutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataTeamOutboxRepository
        extends JpaRepository<TeamOutboxJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT outbox
            FROM TeamOutboxJpaEntity outbox
            WHERE outbox.id = :outboxId
            """)
    Optional<TeamOutboxJpaEntity> findByIdForUpdate(
            @Param("outboxId") Long outboxId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "-2"
            )
    )
    @Query("""
            SELECT outbox
            FROM TeamOutboxJpaEntity outbox
            WHERE (
                    outbox.status IN :retryableStatuses
                    AND outbox.nextRetryAt <= :now
                  )
               OR (
                    outbox.status = :processingStatus
                    AND outbox.updatedAt <= :processingTimeoutAt
                  )
            ORDER BY outbox.nextRetryAt ASC, outbox.id ASC
            """)
    List<TeamOutboxJpaEntity> findRetryTargets(
            @Param("retryableStatuses") List<TeamOutboxStatus> retryableStatuses,
            @Param("processingStatus") TeamOutboxStatus processingStatus,
            @Param("now") LocalDateTime now,
            @Param("processingTimeoutAt") LocalDateTime processingTimeoutAt,
            Pageable pageable
    );
}