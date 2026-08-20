package com.ohgiraffer.team.application.usecase;

import java.time.LocalDate;

public record UserTeamHistoryResult(
        Long teamId,
        String teamName,
        LocalDate startDate,
        LocalDate endDate
) {
}