package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamMemberResult;

import java.time.LocalDateTime;

public record TeamMemberResponse(
        Long teamMemberId,
        Long userId,
        String userName,
        String email,
        LocalDateTime joinedAt
) {

    public static TeamMemberResponse from(
            TeamMemberResult result
    ) {
        return new TeamMemberResponse(
                result.teamMemberId(),
                result.userId(),
                result.userName(),
                result.email(),
                result.joinedAt()
        );
    }
}