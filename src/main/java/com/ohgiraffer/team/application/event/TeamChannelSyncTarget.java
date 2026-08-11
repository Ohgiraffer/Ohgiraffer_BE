package com.ohgiraffer.team.application.event;

import java.util.List;
import java.util.Objects;

public record TeamChannelSyncTarget(
        Long teamId,
        List<Long> memberUserIds
) {

    public TeamChannelSyncTarget {
        if (teamId == null
                || teamId <= 0) {
            throw new IllegalArgumentException(
                    "teamId가 올바르지 않습니다."
            );
        }

        memberUserIds =
                memberUserIds == null
                        ? List.of()
                        : memberUserIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
    }
}