package com.ohgiraffer.team.application.event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TeamConfigurationSavedEvent(
        List<TeamChannelSyncTarget> channelSyncTargets,
        List<TeamWorkspaceSyncTarget> workspaceSyncTargets,
        boolean createChatChannel,
        boolean createNotionPage
) {

    public TeamConfigurationSavedEvent {
        channelSyncTargets =
                mergeChannelTargetsByTeamId(
                        channelSyncTargets
                );

        workspaceSyncTargets =
                mergeWorkspaceTargetsByTeamId(
                        workspaceSyncTargets
                );
    }

    private static List<TeamChannelSyncTarget> mergeChannelTargetsByTeamId(
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

    private static List<TeamWorkspaceSyncTarget> mergeWorkspaceTargetsByTeamId(
            List<TeamWorkspaceSyncTarget> targets
    ) {
        if (targets == null
                || targets.isEmpty()) {
            return List.of();
        }

        Map<Long, TeamWorkspaceSyncTarget> targetByTeamId =
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