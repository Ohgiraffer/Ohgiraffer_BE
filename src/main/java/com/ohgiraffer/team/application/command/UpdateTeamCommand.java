package com.ohgiraffer.team.application.command;

import java.time.LocalDate;

public record UpdateTeamCommand(
        Long teamId,
        Long requesterId,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}