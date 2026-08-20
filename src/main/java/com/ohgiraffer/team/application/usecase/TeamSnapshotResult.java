package com.ohgiraffer.team.application.usecase;

import java.util.List;

public record TeamSnapshotResult(
        Long teamId,
        String teamName,
        int memberCount,
        List<TeamSnapshotMemberResult> members
) {
}