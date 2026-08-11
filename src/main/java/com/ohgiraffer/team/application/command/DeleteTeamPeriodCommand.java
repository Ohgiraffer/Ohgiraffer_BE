package com.ohgiraffer.team.application.command;

public record DeleteTeamPeriodCommand(
        Long requesterId,
        Long teamPeriodId
) {
}