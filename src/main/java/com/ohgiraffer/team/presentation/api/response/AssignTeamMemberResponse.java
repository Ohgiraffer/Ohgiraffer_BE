package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.AssignTeamMemberResult;

import java.time.LocalDateTime;

public record AssignTeamMemberResponse(
        Long teamMemberId,
        Long teamId,
        Long userId,
        String userName,
        String email,
        LocalDateTime joinedAt
) {

    public static AssignTeamMemberResponse from(
            AssignTeamMemberResult result
    ) {
        return new AssignTeamMemberResponse(
                result.teamMemberId(),
                result.teamId(),
                result.userId(),
                result.userName(),
                result.email(),
                result.joinedAt()
        );
    }
}