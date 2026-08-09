package com.ohgiraffer.team.application.command;

import java.util.List;

public record SaveTeamAssignmentsCommand(
        Long requesterId,
        List<TeamAssignmentCommand> teams,
        List<Long> unassignedUserIds
) {
}