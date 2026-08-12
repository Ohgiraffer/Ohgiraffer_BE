package com.ohgiraffer.team.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamOutboxRetryScheduler {

    private final TeamOutboxProcessor teamOutboxProcessor;

    @Scheduled(
            fixedDelayString = "${team.outbox.retry-fixed-delay-ms:60000}",
            initialDelayString = "${team.outbox.retry-initial-delay-ms:30000}"
    )
    public void retryFailedOutboxJobs() {
        try {
            teamOutboxProcessor.processRetryTargets();
        } catch (RuntimeException exception) {
            log.error(
                    "[TeamOutbox] 재시도 스케줄러 실행 실패",
                    exception
            );
        }
    }
}