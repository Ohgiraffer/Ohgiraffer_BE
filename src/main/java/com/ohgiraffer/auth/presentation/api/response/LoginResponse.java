package com.ohgiraffer.auth.presentation.api.response;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;

public record LoginResponse(
        String accessToken,
        Role role,
        UserStatus status,
        Long bootcampId,
        boolean need_reset_pw
) {
    public static LoginResponse of(String accessToken,  Role role, UserStatus status, Long bootcampId, boolean need_reset_pw) {
        return new LoginResponse(accessToken, role, status, bootcampId, need_reset_pw);
    }
}
