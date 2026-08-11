package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.UpdateTeamPeriodCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface UpdateTeamPeriodUseCase {

    TeamPeriodResult updateTeamPeriod(
            UpdateTeamPeriodCommand command,
            Role requesterRole
    );
}