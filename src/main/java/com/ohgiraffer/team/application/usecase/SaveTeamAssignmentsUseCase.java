package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.SaveTeamAssignmentsCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface SaveTeamAssignmentsUseCase {

    void saveTeamAssignments(
            SaveTeamAssignmentsCommand command,
            Role requesterRole
    );
}