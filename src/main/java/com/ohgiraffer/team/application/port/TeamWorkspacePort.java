package com.ohgiraffer.team.application.port;

import java.util.List;
import java.util.Optional;

public interface TeamWorkspacePort {

    Optional<String> findPageIdByTeamId(Long teamId);

    String createTeamPage(
            Long teamId,
            String teamName,
            List<String> memberNames
    );

    void updateTeamPage(
            String notionPageId,
            Long teamId,
            String teamName,
            List<String> memberNames
    );
}