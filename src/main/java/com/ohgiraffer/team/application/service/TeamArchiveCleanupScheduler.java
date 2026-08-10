package com.ohgiraffer.team.application.service;

import com.ohgiraffer.team.domain.model.TeamPeriod;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamArchiveCleanupScheduler {

    private final TeamPeriodRepository teamPeriodRepository;
    private final Clock clock;

    @Scheduled(
            cron = "0 0 3 * * *",
            zone = "Asia/Seoul"
    )
    @Transactional
    public void archiveAndCleanupTeamPeriods() {
        LocalDateTime now =
                LocalDateTime.now(
                        clock
                );

        LocalDate today =
                now.toLocalDate();

        archiveExpiredPeriods(
                today,
                now
        );

        cleanupOldArchivedPeriods(
                now
        );
    }

    private void archiveExpiredPeriods(
            LocalDate today,
            LocalDateTime archivedAt
    ) {
        List<TeamPeriod> archivablePeriods =
                teamPeriodRepository.findArchivablePeriodsForUpdate(
                        today
                );

        archivablePeriods.stream()
                .map(period -> period.archive(
                        archivedAt
                ))
                .forEach(teamPeriodRepository::save);
    }

    private void cleanupOldArchivedPeriods(
            LocalDateTime now
    ) {
        LocalDateTime deleteThreshold =
                now.minusMonths(
                        1
                );

        List<TeamPeriod> deletablePeriods =
                teamPeriodRepository.findDeletableArchivedPeriodsForUpdate(
                        deleteThreshold
                );

        deletablePeriods.stream()
                .map(period -> period.markDeleted(
                        now
                ))
                .forEach(teamPeriodRepository::save);
    }
}