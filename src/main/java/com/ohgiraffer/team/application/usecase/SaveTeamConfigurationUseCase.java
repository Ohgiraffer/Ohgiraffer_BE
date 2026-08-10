package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.SaveTeamConfigurationCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface SaveTeamConfigurationUseCase {

    void saveTeamConfiguration(
            SaveTeamConfigurationCommand command,
            Role requesterRole
    );
}