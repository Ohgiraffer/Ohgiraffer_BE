package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.team.domain.model.Team;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TeamDetailResult(
        Long teamId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean dissolved,
        LocalDateTime dissolvedAt,
        String sendbirdChannelUrl,
        String notionPageId,
        int memberCount,
        List<TeamMemberResult> members
) {

    public static TeamDetailResult of(
            Team team,
            List<TeamMemberResult> members
    ) {
        return new TeamDetailResult(
                team.getId(),
                team.getName(),
                team.getStartDate(),
                team.getEndDate(),
                team.isDissolved(),
                team.getDissolvedAt(),
                team.getSendbirdChannelUrl(),
                team.getNotionPageId(),
                members.size(),
                members
        );
    }
}