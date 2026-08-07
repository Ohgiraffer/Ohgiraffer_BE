package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.Team;

import java.time.LocalDate;
import java.util.List;

public record TeamListResult(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved,
        int memberCount,
        List<TeamMemberResult> members
) {

    public static TeamListResult of(
            Team team,
            List<TeamMemberResult> members
    ) {
        return new TeamListResult(
                team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.isDissolved(),
                members.size(),
                members
        );
    }
}