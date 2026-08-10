package com.ohgiraffer.team.application.command;

import java.time.LocalDate;

public record CreateTeamPeriodCommand(
        Long requesterId,
        LocalDate startDate,
        LocalDate endDate
) {
}