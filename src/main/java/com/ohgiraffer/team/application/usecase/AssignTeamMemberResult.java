package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.user.domain.model.User;

import java.time.LocalDateTime;

public record AssignTeamMemberResult(
        Long teamMemberId,
        Long teamId,
        Long userId,
        String userName,
        String email,
        LocalDateTime joinedAt
) {

    public static AssignTeamMemberResult of(
            TeamMember teamMember,
            User user
    ) {
        return new AssignTeamMemberResult(
                teamMember.getId(),
                teamMember.getTeamId(),
                user.getId(),
                user.getName(),
                user.getEmail(),
                teamMember.getJoinedAt()
        );
    }
}