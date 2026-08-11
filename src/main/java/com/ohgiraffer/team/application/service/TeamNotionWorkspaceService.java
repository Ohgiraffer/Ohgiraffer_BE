package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.aop.lock.DistributedLock;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.port.TeamWorkspacePort;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TeamNotionWorkspaceService {

    private final TeamRepository teamRepository;
    private final TeamWorkspacePort teamWorkspacePort;
    private final TeamNotionPageUpdater teamNotionPageUpdater;

    @DistributedLock(
            key = "'team:notion:workspace:' + #teamId",
            waitTime = 10,
            leaseTime = 60,
            timeUnit = TimeUnit.SECONDS
    )
    public void syncTeamWorkspace(Long teamId) {
        Team team =
                teamRepository.findById(teamId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        if (team.isDeleted()) {
            return;
        }

        List<String> memberNames =
                teamRepository.findActiveMembersByTeamId(teamId)
                        .stream()
                        .map(TeamMember::getUserName)
                        .filter(this::hasText)
                        .map(String::trim)
                        .distinct()
                        .toList();

        if (hasText(team.getNotionPageId())) {
            teamWorkspacePort.updateTeamPage(
                    team.getNotionPageId(),
                    team.getId(),
                    team.getName(),
                    memberNames
            );
            return;
        }

        Optional<String> existingPageId =
                teamWorkspacePort.findPageIdByTeamId(team.getId());

        if (existingPageId.isPresent()) {
            String notionPageId =
                    existingPageId.get();

            teamNotionPageUpdater.updateNotionPageId(
                    team.getId(),
                    notionPageId
            );

            teamWorkspacePort.updateTeamPage(
                    notionPageId,
                    team.getId(),
                    team.getName(),
                    memberNames
            );
            return;
        }

        String createdPageId =
                teamWorkspacePort.createTeamPage(
                        team.getId(),
                        team.getName(),
                        memberNames
                );

        teamNotionPageUpdater.updateNotionPageId(
                team.getId(),
                createdPageId
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}