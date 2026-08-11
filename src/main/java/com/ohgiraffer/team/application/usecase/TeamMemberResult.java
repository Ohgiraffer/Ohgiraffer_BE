package com.ohgiraffer.team.application.usecase;

import java.time.LocalDateTime;

public record TeamMemberResult(
        Long teamMemberId,
        Long userId,
        String userName,
        String email,
        String profileImgUrl,
        LocalDateTime joinedAt
) {
}