package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.CreateTeamResult;

import java.time.LocalDate;
import java.util.List;

public record CreateTeamResponse(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved,
        int memberCount,
        List<TeamMemberResponse> members
) {

    public static CreateTeamResponse from(
            CreateTeamResult result
    ) {
        return new CreateTeamResponse(
                result.teamId(),
                result.name(),
                result.startDate(),
                result.endDate(),
                result.dissolved(),
                0,
                List.of()
        );
    }
}