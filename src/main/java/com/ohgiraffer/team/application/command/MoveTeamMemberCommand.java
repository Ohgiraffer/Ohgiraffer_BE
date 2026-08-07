package com.ohgiraffer.team.application.command;

public record MoveTeamMemberCommand(
        Long sourceTeamId,
        Long targetTeamId,
        Long teamMemberId,
        Long requesterId
) {
}