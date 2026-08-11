package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamMemberResult;
import com.ohgiraffer.user.domain.model.Role;

import java.time.LocalDateTime;

public record TeamMemberResponse(
        Long teamMemberId,
        Long userId,
        String userName,
        String email,
        String profileImgUrl,
        LocalDateTime joinedAt
) {

    public static TeamMemberResponse from(
            TeamMemberResult result,
            Role requesterRole
    ) {
        return new TeamMemberResponse(
                result.teamMemberId(),
                result.userId(),
                result.userName(),
                maskEmail(
                        result.email(),
                        requesterRole
                ),
                result.profileImgUrl(),
                result.joinedAt()
        );
    }

    private static String maskEmail(
            String email,
            Role requesterRole
    ) {
        if (requesterRole == Role.STUDENT) {
            return null;
        }

        return email;
    }
}