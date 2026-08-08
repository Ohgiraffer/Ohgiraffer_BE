package com.ohgiraffer.team.application.command;

public record AssignTeamMemberCommand(
        Long teamId,
        Long requesterId,
        Long userId
) {
}