package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.TeamMember;

import java.time.LocalDateTime;

public record TeamMemberResult(
        Long teamMemberId,
        Long userId,
        String userName,
        String email,
        LocalDateTime joinedAt
) {

    public static TeamMemberResult from(
            TeamMember member
    ) {
        return new TeamMemberResult(
                member.getId(),
                member.getUserId(),
                member.getUserName(),
                member.getEmail(),
                member.getJoinedAt()
        );
    }
}