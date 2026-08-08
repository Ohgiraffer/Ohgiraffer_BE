package com.ohgiraffer.space.presentation.api.response;

import com.ohgiraffer.space.application.usecase.SpaceOccupantResult;
import com.ohgiraffer.user.domain.model.Role;

public record SpaceOccupantResponse(
        Long userId,
        String userName,
        Role role,
        boolean mine
) {

    public static SpaceOccupantResponse from(
            SpaceOccupantResult result
    ) {
        return new SpaceOccupantResponse(
                result.userId(),
                result.userName(),
                result.role(),
                result.mine()
        );
    }
}