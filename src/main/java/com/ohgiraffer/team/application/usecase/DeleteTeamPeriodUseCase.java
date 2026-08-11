package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.DeleteTeamPeriodCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface DeleteTeamPeriodUseCase {

    void deleteTeamPeriod(
            DeleteTeamPeriodCommand command,
            Role requesterRole
    );
}