package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamPeriodResult;

import java.time.LocalDate;

public record CreateTeamPeriodResponse(
        Long teamPeriodId,
        LocalDate startDate,
        LocalDate endDate
) {

    public static CreateTeamPeriodResponse from(
            TeamPeriodResult result
    ) {
        return new CreateTeamPeriodResponse(
                result.teamPeriodId(),
                result.startDate(),
                result.endDate()
        );
    }
}