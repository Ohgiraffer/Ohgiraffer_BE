package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxStatus;
import com.ohgiraffer.team.domain.repository.TeamOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamOutboxRepositoryAdapter
        implements TeamOutboxRepository {

    private final SpringDataTeamOutboxRepository springDataTeamOutboxRepository;

    @Override
    public TeamOutbox save(
            TeamOutbox outbox
    ) {
        return springDataTeamOutboxRepository.saveAndFlush(
                        TeamOutboxJpaEntity.from(
                                outbox
                        )
                )
                .toDomain();
    }

    @Override
    public Optional<TeamOutbox> findByIdForUpdate(
            Long outboxId
    ) {
        return springDataTeamOutboxRepository.findByIdForUpdate(
                        outboxId
                )
                .map(TeamOutboxJpaEntity::toDomain);
    }

    @Override
    public List<TeamOutbox> findRetryTargets(
            List<TeamOutboxStatus> retryableStatuses,
            TeamOutboxStatus processingStatus,
            LocalDateTime now,
            LocalDateTime processingTimeoutAt,
            int limit
    ) {
        return springDataTeamOutboxRepository.findRetryTargets(
                        retryableStatuses,
                        processingStatus,
                        now,
                        processingTimeoutAt,
                        PageRequest.of(
                                0,
                                limit
                        )
                )
                .stream()
                .map(TeamOutboxJpaEntity::toDomain)
                .toList();
    }
}