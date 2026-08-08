package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.application.port.SpaceOccupantData;
import com.ohgiraffer.user.domain.model.Role;

public record SpaceOccupantResult(
        Long userId,
        String userName,
        Role role,
        boolean mine
) {

    public static SpaceOccupantResult from(
            SpaceOccupantData data,
            Long requesterId
    ) {
        return new SpaceOccupantResult(
                data.userId(),
                data.userName(),
                data.role(),
                data.userId().equals(requesterId)
        );
    }
}