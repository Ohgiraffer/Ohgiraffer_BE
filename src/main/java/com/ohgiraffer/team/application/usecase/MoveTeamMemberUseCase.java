package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.MoveTeamMemberCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface MoveTeamMemberUseCase {

    AssignTeamMemberResult moveTeamMember(
            MoveTeamMemberCommand command,
            Role requesterRole
    );
}