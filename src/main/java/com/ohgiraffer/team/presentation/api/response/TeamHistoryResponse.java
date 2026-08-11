package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamHistoryResult;

import java.time.LocalDate;
import java.util.List;

public record TeamHistoryResponse(
        LocalDate snapshotDate,
        List<TeamSnapshotResponse> teams,
        List<TeamChangeHistoryResponse> histories
) {

    public static TeamHistoryResponse from(
            TeamHistoryResult result
    ) {
        return new TeamHistoryResponse(
                result.snapshotDate(),
                result.teams()
                        .stream()
                        .map(TeamSnapshotResponse::from)
                        .toList(),
                result.histories()
                        .stream()
                        .map(TeamChangeHistoryResponse::from)
                        .toList()
        );
    }
}