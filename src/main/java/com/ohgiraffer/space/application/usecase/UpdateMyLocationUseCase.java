package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.application.command.UpdateMyLocationCommand;
import com.ohgiraffer.user.domain.model.Role;

public interface UpdateMyLocationUseCase {

    MyLocationResult update(
            UpdateMyLocationCommand command,
            Long requesterId,
            Role requesterRole
    );
}