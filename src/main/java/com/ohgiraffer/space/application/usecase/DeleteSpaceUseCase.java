package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

public interface DeleteSpaceUseCase {

    void delete(
            Long spaceId,
            Long requesterId,
            Role requesterRole
    );
}