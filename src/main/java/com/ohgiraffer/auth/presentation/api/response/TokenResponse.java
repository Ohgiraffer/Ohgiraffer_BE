package com.ohgiraffer.auth.presentation.api.response;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;

public record TokenResponse(
        Long userId,
        String accessToken,
        Role role,
        UserStatus status
) {
}
