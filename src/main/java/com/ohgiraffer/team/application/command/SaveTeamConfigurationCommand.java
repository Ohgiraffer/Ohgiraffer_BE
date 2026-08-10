package com.ohgiraffer.team.application.command;

import java.util.List;

public record SaveTeamConfigurationCommand(
        Long requesterId,
        Long teamPeriodId,
        List<TeamConfigurationCommand> teams,
        List<Long> deletedTeamIds,
        List<Long> unassignedUserIds,
        boolean createChatChannel,
        boolean createNotionPage
) {

    public SaveTeamConfigurationCommand {
        teams =
                teams == null
                        ? List.of()
                        : List.copyOf(
                        teams
                );

        deletedTeamIds =
                deletedTeamIds == null
                        ? List.of()
                        : List.copyOf(
                        deletedTeamIds
                );

        unassignedUserIds =
                unassignedUserIds == null
                        ? List.of()
                        : List.copyOf(
                        unassignedUserIds
                );
    }
}