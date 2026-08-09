package com.ohgiraffer.user.presentation.api.response;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;

public record UserListResponse(
        Long userId,
        String name,
        String email,
        Role role,
        String teamName,
        UserStatus status
) {
    public static UserListResponse of(User user, String teamName) {
        return new UserListResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                teamName,
                user.getStatus()
        );
    }
}