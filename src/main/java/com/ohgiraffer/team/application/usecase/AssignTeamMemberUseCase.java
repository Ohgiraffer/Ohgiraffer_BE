package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.AssignTeamMemberCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface AssignTeamMemberUseCase {

    AssignTeamMemberResult assignTeamMember(
            AssignTeamMemberCommand command,
            Role requesterRole
    );
}