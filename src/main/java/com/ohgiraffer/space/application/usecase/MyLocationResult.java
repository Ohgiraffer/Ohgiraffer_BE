package com.ohgiraffer.space.application.usecase;

import com.ohgiraffer.space.domain.model.Space;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.Role;

public record MyLocationResult(
        Long userId,
        String userName,
        Role role,
        Long spaceId,
        String spaceName
) {

    public static MyLocationResult located(
            User user,
            Space space
    ) {
        return new MyLocationResult(
                user.getId(),
                user.getName(),
                user.getRole(),
                space.getId(),
                space.getName()
        );
    }

    public static MyLocationResult cleared(
            User user
    ) {
        return new MyLocationResult(
                user.getId(),
                user.getName(),
                user.getRole(),
                null,
                null
        );
    }
}