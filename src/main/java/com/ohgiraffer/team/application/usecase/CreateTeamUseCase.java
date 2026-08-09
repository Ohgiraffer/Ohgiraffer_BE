package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.CreateTeamCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface CreateTeamUseCase {

    CreateTeamResult createTeam(
            CreateTeamCommand command,
            Role requesterRole
    );
}