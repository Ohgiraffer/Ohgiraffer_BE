package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface GetTeamDetailUseCase {

    TeamDetailResult getTeam(
            Long teamId,
            Long requesterId,
            Role requesterRole
    );
}