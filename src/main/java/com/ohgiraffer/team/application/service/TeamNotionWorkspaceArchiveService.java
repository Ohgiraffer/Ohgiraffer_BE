package com.ohgiraffer.team.application.service;

import com.ohgiraffer.team.application.port.TeamWorkspacePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamNotionWorkspaceArchiveService {

    private final TeamWorkspacePort teamWorkspacePort;

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
}