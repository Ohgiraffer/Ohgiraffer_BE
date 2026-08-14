package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamWorkspaceResult;

public record TeamWorkspaceResponse(
        Long teamId,
        String notionPageId,
        String workspaceUrl
) {

    public static TeamWorkspaceResponse from(
            TeamWorkspaceResult result
    ) {
        return new TeamWorkspaceResponse(
                result.teamId(),
                result.notionPageId(),
                result.workspaceUrl()
        );
    }
}