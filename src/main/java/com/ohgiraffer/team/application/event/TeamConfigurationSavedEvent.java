package com.ohgiraffer.team.application.event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TeamConfigurationSavedEvent(
        List<TeamChannelSyncTarget> channelSyncTargets
) {

    public TeamConfigurationSavedEvent {
        channelSyncTargets =
                mergeTargetsByTeamId(
                        channelSyncTargets
                );
    }

    private static List<TeamChannelSyncTarget> mergeTargetsByTeamId(
            List<TeamChannelSyncTarget> targets
    ) {
        if (targets == null
                || targets.isEmpty()) {
            return List.of();
        }

        Map<Long, TeamChannelSyncTarget> targetByTeamId =
                new LinkedHashMap<>();

        targets.forEach(target ->
                targetByTeamId.put(
                        target.teamId(),
                        target
                )
        );

        return List.copyOf(
                targetByTeamId.values()
        );
    }
}