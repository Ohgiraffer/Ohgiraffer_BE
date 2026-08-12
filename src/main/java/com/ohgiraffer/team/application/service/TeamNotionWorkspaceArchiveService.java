package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.port.TeamWorkspacePort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamNotionWorkspaceArchiveService {

    private static final String TEAM_NOTION_CIRCUIT_BREAKER =
            "teamNotion";

    private final TeamWorkspacePort teamWorkspacePort;

    @CircuitBreaker(
            name = TEAM_NOTION_CIRCUIT_BREAKER,
            fallbackMethod = "fallbackOnNotionArchiveFailure"
    )
    public void archive(
            String notionPageId
    ) {
        if (notionPageId == null
                || notionPageId.isBlank()) {
            return;
        }

        teamWorkspacePort.archiveTeamPage(
                notionPageId.trim()
        );
    }

    private void fallbackOnNotionArchiveFailure(
            String notionPageId,
            Throwable throwable
    ) {
        if (isNotionApiFailure(
                throwable
        )) {
            log.warn(
                    "[Team] Notion 팀 페이지 보관 실패 또는 서킷 오픈 | notionPageId={}, cause={}",
                    notionPageId,
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
}