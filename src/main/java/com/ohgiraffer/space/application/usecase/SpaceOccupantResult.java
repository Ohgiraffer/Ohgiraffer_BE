package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.application.port.SpaceOccupantData;
import com.ohgiraffer.user.domain.model.Role;

public record SpaceOccupantResult(
        Long userId,
        String userName,
        Role role,
        String profileImgUrl,
        boolean mine
) {

    public static SpaceOccupantResult from(
            SpaceOccupantData data,
            Long requesterId,
            String profileImgUrl
    ) {
        return new SpaceOccupantResult(
                data.userId(),
                data.userName(),
                data.role(),
                profileImgUrl,
                data.userId().equals(requesterId)
        );
    }
}