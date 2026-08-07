package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamListResult;

import java.util.List;

public record TeamListResponse(
        List<TeamListItemResponse> teams
) {

    public static TeamListResponse from(
            List<TeamListResult> results
    ) {
        return new TeamListResponse(
                results.stream()
                        .map(TeamListItemResponse::from)
                        .toList()
        );
    }
}