package com.ohgiraffer.team.application.event;

import java.util.List;

public record TeamPeriodDeletedEvent(
        Long teamPeriodId,
        List<TeamExternalResourceDeleteTarget> targets
) {

    public TeamPeriodDeletedEvent {
        if (teamPeriodId == null || teamPeriodId <= 0) {
            throw new IllegalArgumentException("teamPeriodId가 올바르지 않습니다.");
        }

        targets = targets == null ? List.of() : List.copyOf(targets);
    }
}