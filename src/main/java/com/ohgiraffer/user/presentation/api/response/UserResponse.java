package com.ohgiraffer.user.presentation.api.response;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;

import java.time.LocalDate;

public record UserResponse(
        Long userId,
        String name,
        String phone,
        String email,
        Role role,
        String profileImgUrl,
        LocalDate joinDate,
        UserStatus status,
        boolean notificationOn,
        Long bootcampId,
        boolean needResetPw
) {
    public static UserResponse from(User user, String profileImgUrl) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                profileImgUrl,
                user.getJoinDate(),
                user.getStatus(),
                user.isNotificationOn(),
                user.getBootcampId(),
                user.isNeedResetPw()
        );
    }
}