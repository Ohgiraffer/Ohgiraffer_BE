package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.aop.lock.DistributedLock;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.port.TeamWorkspacePort;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamNotionWorkspaceService {

    private static final String TEAM_NOTION_CIRCUIT_BREAKER =
            "teamNotion";

    private final TeamRepository teamRepository;
    private final TeamWorkspacePort teamWorkspacePort;
    private final TeamNotionPageUpdater teamNotionPageUpdater;

    @DistributedLock(
            key = "'team:notion:workspace:' + #teamId",
            waitTime = 30,
            leaseTime = 300,
            timeUnit = TimeUnit.SECONDS
    )
    @CircuitBreaker(
            name = TEAM_NOTION_CIRCUIT_BREAKER,
            fallbackMethod = "fallbackOnNotionFailure"
    )
    public void syncTeamWorkspace(
            Long teamId
    ) {
        Team team =
                teamRepository.findById(
                                teamId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        if (team.isDeleted()) {
            return;
        }

        List<String> memberNames =
                teamRepository.findActiveMembersByTeamId(
                                teamId
                        )
                        .stream()
                        .map(
                                TeamMember::getUserName
                        )
                        .filter(
                                this::hasText
                        )
                        .map(
                                String::trim
                        )
                        .distinct()
                        .toList();

        if (hasText(
                team.getNotionPageId()
        )) {
            teamWorkspacePort.updateTeamPage(
                    team.getNotionPageId(),
                    team.getId(),
                    team.getName(),
                    memberNames
            );
            return;
        }

        Optional<String> existingPageId =
                teamWorkspacePort.findPageIdByTeamId(
                        team.getId()
                );

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

        teamWorkspacePort.updateTeamPage(
                createdPageId,
                team.getId(),
                team.getName(),
                memberNames
        );
    }

    private void fallbackOnNotionFailure(
            Long teamId,
            Throwable throwable
    ) {
        if (isNotionApiFailure(
                throwable
        )) {
            log.warn(
                    "[Team] Notion 팀 페이지 동기화 실패 또는 서킷 오픈 | teamId={}, cause={}",
                    teamId,
                    throwable.toString()
            );

            throw new BusinessException(
                    ErrorCode.TEAM_NOTION_API_ERROR
            );
        }

        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new BusinessException(
                ErrorCode.INTERNAL_SERVER_ERROR,
                throwable
        );
    }

    private boolean isNotionApiFailure(
            Throwable throwable
    ) {
        if (throwable instanceof CallNotPermittedException) {
            return true;
        }

        return throwable instanceof BusinessException businessException
                && businessException.getErrorCode()
                == ErrorCode.TEAM_NOTION_API_ERROR;
    }

    private boolean hasText(
            String value
    ) {
        return value != null && !value.isBlank();
    }
}