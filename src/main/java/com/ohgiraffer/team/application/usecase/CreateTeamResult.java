package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.Team;

import java.time.LocalDate;

public record CreateTeamResult(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved
) {

    public static CreateTeamResult from(
            Team team
    ) {
        return new CreateTeamResult(
                team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.isDissolved()
        );
    }
}