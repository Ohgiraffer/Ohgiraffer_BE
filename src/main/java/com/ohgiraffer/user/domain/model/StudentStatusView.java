package com.ohgiraffer.user.domain.model;

public record StudentStatusView(
        Long userId,
        UserStatus status
) {
}
