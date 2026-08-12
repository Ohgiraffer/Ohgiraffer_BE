package com.ohgiraffer.consultation.domain.model;

import com.ohgiraffer.user.domain.model.Role;

public record UserSummary(
        Long userId,
        String name,
        Role role,
        String profileImgUrl
) {
}
