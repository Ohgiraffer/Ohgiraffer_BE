package com.ohgiraffer.team.application.service;

import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamArchiveCleanupScheduler {

    private final TeamRepository teamRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void archiveAndCleanupTeams() {
        LocalDate today =
                LocalDate.now();

        LocalDateTime now =
                LocalDateTime.now();

        archiveExpiredTeams(
                today,
                now
        );

        cleanupOldArchivedTeams(
                now
        );
    }

    private void archiveExpiredTeams(
            LocalDate today,
            LocalDateTime archivedAt
    ) {
        List<Team> archivableTeams =
                teamRepository.findArchivableTeamsForUpdate(
                        today
                );

        archivableTeams.stream()
                .map(team -> team.archive(
                        archivedAt
                ))
                .forEach(teamRepository::save);
    }

    private void cleanupOldArchivedTeams(
            LocalDateTime now
    ) {
        LocalDateTime deleteThreshold =
                now.minusMonths(
                        1
                );

        List<Team> deletableTeams =
                teamRepository.findDeletableArchivedTeamsForUpdate(
                        deleteThreshold
                );

        deletableTeams.stream()
                .map(team -> markExternalResourcesDeleted(
                        team,
                        now
                ))
                .map(team -> team.markDeleted(
                        now
                ))
                .forEach(teamRepository::save);
    }

    private Team markExternalResourcesDeleted(
            Team team,
            LocalDateTime deletedAt
    ) {
        Team updatedTeam =
                team;

        if (team.getSendbirdChannelUrl() != null
                && !team.getSendbirdChannelUrl()
                .isBlank()) {
            updatedTeam =
                    updatedTeam.markChannelDeleted(
                            deletedAt
                    );
        }

        if (team.getNotionPageId() != null
                && !team.getNotionPageId()
                .isBlank()) {
            updatedTeam =
                    updatedTeam.markWorkspaceDeleted(
                            deletedAt
                    );
        }

        return updatedTeam;
    }
}