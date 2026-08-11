package com.ohgiraffer.team.application.command;

import java.time.LocalDate;

public record UpdateTeamPeriodCommand(
        Long requesterId,
        Long teamPeriodId,
        LocalDate startDate,
        LocalDate endDate
) {
}