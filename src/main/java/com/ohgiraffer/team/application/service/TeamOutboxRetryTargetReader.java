package com.ohgiraffer.team.application.service;

import com.ohgiraffer.team.domain.model.TeamOutbox;
import com.ohgiraffer.team.domain.model.TeamOutboxStatus;
import com.ohgiraffer.team.domain.repository.TeamOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamOutboxRetryTargetReader {

    private static final int RETRY_BATCH_SIZE =
            20;

    private static final Duration PROCESSING_TIMEOUT =
            Duration.ofMinutes(
                    10
            );

    private final TeamOutboxRepository teamOutboxRepository;
    private final Clock clock;

    @Transactional
    public List<Long> findRetryTargetIds() {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        LocalDateTime processingTimeoutAt =
                now.minus(
                        PROCESSING_TIMEOUT
                );

        return teamOutboxRepository.findRetryTargets(
                        List.of(
                                TeamOutboxStatus.PENDING,
                                TeamOutboxStatus.FAILED
                        ),
                        TeamOutboxStatus.PROCESSING,
                        now,
                        processingTimeoutAt,
                        RETRY_BATCH_SIZE
                )
                .stream()
                .map(
                        TeamOutbox::getId
                )
                .toList();
    }
}