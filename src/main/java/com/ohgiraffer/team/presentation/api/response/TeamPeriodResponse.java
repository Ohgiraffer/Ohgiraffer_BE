package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamPeriodResult;

import java.time.LocalDate;

public record TeamPeriodResponse(
        Long teamPeriodId,
        LocalDate startDate,
        LocalDate endDate
) {

    public static TeamPeriodResponse from(
            TeamPeriodResult result
    ) {
        return new TeamPeriodResponse(
                result.teamPeriodId(),
                result.startDate(),
                result.endDate()
        );
    }
}