package com.ohgiraffer.team.application.command;

import java.util.List;

public record TeamAssignmentCommand(
        Long teamId,
        List<Long> userIds
) {
}