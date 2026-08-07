package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.UpdateTeamCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface UpdateTeamUseCase {

    TeamDetailResult updateTeam(
            UpdateTeamCommand command,
            Role requesterRole
    );
}