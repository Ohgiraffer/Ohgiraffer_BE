package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.CreateTeamPeriodCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface CreateTeamPeriodUseCase {

    TeamPeriodResult createTeamPeriod(
            CreateTeamPeriodCommand command,
            Role requesterRole
    );
}