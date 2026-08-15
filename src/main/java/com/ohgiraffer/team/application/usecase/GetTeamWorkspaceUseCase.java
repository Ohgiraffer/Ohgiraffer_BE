package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetTeamWorkspaceUseCase {

    TeamWorkspaceResult getTeamWorkspace(
            Long requesterId,
            Role requesterRole,
            Long teamId
    );
}