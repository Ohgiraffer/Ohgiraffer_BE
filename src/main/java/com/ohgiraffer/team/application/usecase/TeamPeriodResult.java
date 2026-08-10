package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.TeamPeriod;

import java.time.LocalDate;

public record TeamPeriodResult(
        Long teamPeriodId,
        LocalDate startDate,
        LocalDate endDate
) {

    public static TeamPeriodResult from(
            TeamPeriod teamPeriod
    ) {
        return new TeamPeriodResult(
                teamPeriod.getId(),
                teamPeriod.getStartDate(),
                teamPeriod.getEndDate()
        );
    }
}