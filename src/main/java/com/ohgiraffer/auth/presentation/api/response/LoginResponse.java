package com.ohgiraffer.auth.presentation.api.response;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Role role,
        UserStatus status
) {
    public static LoginResponse of(String accessToken, String refreshToken, Role role, UserStatus status) {
        return new LoginResponse(accessToken, refreshToken, role, status);
    }
}
