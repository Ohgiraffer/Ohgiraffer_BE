package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.UserTeamHistoryResult;

import java.util.List;

public record UserTeamHistoryListResponse(
        List<UserTeamHistoryResponse> histories
) {

    public static UserTeamHistoryListResponse from(
            List<UserTeamHistoryResult> results
    ) {
        return new UserTeamHistoryListResponse(
                results.stream()
                        .map(
                                UserTeamHistoryResponse::from
                        )
                        .toList()
        );
    }
}