package com.ohgiraffer.ai.infrastructure.scheduler;

import com.ohgiraffer.ai.infrastructure.persistence.SpringDataAiUsageLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiUsageRetentionScheduler {

    private final SpringDataAiUsageLogJpaRepository aiUsageLogJpaRepository;

    @Value("${ai.usage.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "0 10 3 * * *")
    @Transactional
    public void purgeOldRawLogs() {
        LocalDateTime cutoff = LocalDate.now().minusDays(retentionDays).atStartOfDay();
        int deleted = aiUsageLogJpaRepository.deleteByCreatedAtBefore(cutoff);
        log.info("AI usage raw log 삭제: cutoff={}, deletedRows={}", cutoff, deleted);
    }
}