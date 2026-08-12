package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamOutboxRepository {

    TeamOutbox save(
            TeamOutbox outbox
    );

    Optional<TeamOutbox> findByIdForUpdate(
            Long outboxId
    );

    List<TeamOutbox> findRetryTargets(
            List<TeamOutboxStatus> retryableStatuses,
            TeamOutboxStatus processingStatus,
            LocalDateTime now,
            LocalDateTime processingTimeoutAt,
            int limit
    );
}