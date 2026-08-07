package com.ohgiraffer.team.presentation.api.response;

import com.ohgiraffer.team.application.usecase.TeamDetailResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TeamDetailResponse(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved,
        LocalDateTime dissolvedAt,
        String sendbirdChannelUrl,
        String notionPageId,
        int memberCount,
        List<TeamMemberResponse> members
) {

    public static TeamDetailResponse from(
            TeamDetailResult result
    ) {
        return new TeamDetailResponse(
                result.teamId(),
                result.name(),
                result.startDate(),
                result.endDate(),
                result.dissolved(),
                result.dissolvedAt(),
                result.sendbirdChannelUrl(),
                result.notionPageId(),
                result.memberCount(),
                result.members()
                        .stream()
                        .map(TeamMemberResponse::from)
                        .toList()
        );
    }
}