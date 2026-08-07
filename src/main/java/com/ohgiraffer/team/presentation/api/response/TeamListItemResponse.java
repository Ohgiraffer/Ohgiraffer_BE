package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamListResult;

import java.time.LocalDate;
import java.util.List;

public record TeamListItemResponse(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved,
        int memberCount,
        List<TeamMemberResponse> members
) {

    public static TeamListItemResponse from(
            TeamListResult result
    ) {
        return new TeamListItemResponse(
                result.teamId(),
                result.name(),
                result.startDate(),
                result.endDate(),
                result.dissolved(),
                result.memberCount(),
                result.members()
                        .stream()
                        .map(TeamMemberResponse::from)
                        .toList()
        );
    }
}