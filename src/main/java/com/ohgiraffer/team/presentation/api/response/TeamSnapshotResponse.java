package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamSnapshotResult;

import java.util.List;

public record TeamSnapshotResponse(
        Long teamId,
        String teamName,
        int memberCount,
        List<TeamSnapshotMemberResponse> members
) {

    public static TeamSnapshotResponse from(
            TeamSnapshotResult result
    ) {
        return new TeamSnapshotResponse(
                result.teamId(),
                result.teamName(),
                result.memberCount(),
                result.members()
                        .stream()
                        .map(TeamSnapshotMemberResponse::from)
                        .toList()
        );
    }
}