package com.ohgiraffer.space.application.port;

import com.ohgiraffer.user.domain.model.Role;

public record SpaceOccupantData(
        Long userId,
        String userName,
        Role role,
        String profileImgKey
) {
}