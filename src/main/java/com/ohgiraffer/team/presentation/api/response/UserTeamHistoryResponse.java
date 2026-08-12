package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.UserTeamHistoryResult;

import java.time.LocalDate;

public record UserTeamHistoryResponse(
        Long teamId,
        String teamName,
        LocalDate startDate,
        LocalDate endDate
) {

    public static UserTeamHistoryResponse from(
            UserTeamHistoryResult result
    ) {
        return new UserTeamHistoryResponse(
                result.teamId(),
                result.teamName(),
                result.startDate(),
                result.endDate()
        );
    }
}