package com.ohgiraffer.team.application.command;

public record RemoveTeamMemberCommand(
        Long teamId,
        Long teamMemberId,
        Long requesterId
) {
}