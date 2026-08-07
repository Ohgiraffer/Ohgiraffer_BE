package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.application.command.RemoveTeamMemberCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface RemoveTeamMemberUseCase {

    void removeTeamMember(
            RemoveTeamMemberCommand command,
            Role requesterRole
    );
}