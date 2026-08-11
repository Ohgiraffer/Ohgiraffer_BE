package com.ohgiraffer.team.application.event;

public record TeamWorkspaceSyncTarget(
        Long teamId
) {

    public TeamWorkspaceSyncTarget {
        if (teamId == null
                || teamId <= 0) {
            throw new IllegalArgumentException(
                    "teamId가 올바르지 않습니다."
            );
        }
    }
}