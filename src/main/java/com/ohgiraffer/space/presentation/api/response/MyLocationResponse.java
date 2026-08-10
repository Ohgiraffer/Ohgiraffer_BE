package com.ohgiraffer.space.presentation.api.response;

import com.ohgiraffer.space.application.usecase.MyLocationResult;
import com.ohgiraffer.user.domain.model.Role;

public record MyLocationResponse(
        Long userId,
        String userName,
        Role role,
        Long spaceId,
        String spaceName
) {

    public static MyLocationResponse from(
            MyLocationResult result
    ) {
        return new MyLocationResponse(
                result.userId(),
                result.userName(),
                result.role(),
                result.spaceId(),
                result.spaceName()
        );
    }
}