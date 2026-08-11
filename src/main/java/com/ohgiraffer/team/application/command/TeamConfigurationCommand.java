package com.ohgiraffer.team.application.command;

import java.util.List;

public record TeamConfigurationCommand(
        Long teamId,
        String name,
        List<Long> userIds
) {

    public TeamConfigurationCommand {
        userIds =
                userIds == null
                        ? List.of()
                        : List.copyOf(userIds);
    }
}