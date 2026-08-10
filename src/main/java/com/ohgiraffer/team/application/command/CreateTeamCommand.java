package com.ohgiraffer.team.application.command;

import java.time.LocalDate;

public record CreateTeamCommand(
        Long requesterId,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}