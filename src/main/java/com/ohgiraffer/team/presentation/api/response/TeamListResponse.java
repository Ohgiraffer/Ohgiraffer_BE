package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public record TeamListResponse(
        List<TeamListItemResponse> teams
) {

    public static TeamListResponse from(
            List<TeamListResult> results,
            Role requesterRole
    ) {
        return new TeamListResponse(
                results.stream()
                        .map(result ->
                                TeamListItemResponse.from(
                                        result,
                                        requesterRole
                                )
                        )
                        .toList()
        );
    }
}