package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamPeriodResult;

import java.util.List;

public record TeamPeriodListResponse(
        List<TeamPeriodResponse> periods
) {

    public static TeamPeriodListResponse from(
            List<TeamPeriodResult> results
    ) {
        return new TeamPeriodListResponse(
                results.stream()
                        .map(TeamPeriodResponse::from)
                        .toList()
        );
    }
}