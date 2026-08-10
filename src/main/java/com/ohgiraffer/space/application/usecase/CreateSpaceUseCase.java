package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.application.command.CreateSpaceCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface CreateSpaceUseCase {

    CreateSpaceResult create(
            CreateSpaceCommand command,
            Long requesterId,
            Role requesterRole
    );
}