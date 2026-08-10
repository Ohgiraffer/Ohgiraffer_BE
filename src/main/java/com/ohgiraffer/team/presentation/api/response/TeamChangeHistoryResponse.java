package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamChangeHistoryResult;

import java.time.LocalDateTime;

public record TeamChangeHistoryResponse(
        Long userId,
        String userName,
        Long fromTeamId,
        String fromTeamName,
        Long toTeamId,
        String toTeamName,
        LocalDateTime changedAt
) {

    public static TeamChangeHistoryResponse from(
            TeamChangeHistoryResult result
    ) {
        return new TeamChangeHistoryResponse(
                result.userId(),
                result.userName(),
                result.fromTeamId(),
                result.fromTeamName(),
                result.toTeamId(),
                result.toTeamName(),
                result.changedAt()
        );
    }
}