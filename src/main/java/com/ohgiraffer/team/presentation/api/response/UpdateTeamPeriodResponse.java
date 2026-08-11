package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamPeriodResult;

import java.time.LocalDate;

public record UpdateTeamPeriodResponse(
        Long teamPeriodId,
        LocalDate startDate,
        LocalDate endDate
) {

    public static UpdateTeamPeriodResponse from(
            TeamPeriodResult result
    ) {
        return new UpdateTeamPeriodResponse(
                result.teamPeriodId(),
                result.startDate(),
                result.endDate()
        );
    }
}