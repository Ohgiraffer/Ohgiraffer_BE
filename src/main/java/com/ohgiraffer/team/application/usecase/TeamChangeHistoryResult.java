package com.ohgiraffer.team.application.usecase;

import java.time.LocalDateTime;

public record TeamChangeHistoryResult(
        Long userId,
        String userName,
        Long fromTeamId,
        String fromTeamName,
        Long toTeamId,
        String toTeamName,
        LocalDateTime changedAt
) {
}