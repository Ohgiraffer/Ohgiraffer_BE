package com.ohgiraffer.team.application.usecase;

import java.time.LocalDate;
import java.util.List;

public record TeamHistoryResult(
        LocalDate snapshotDate,
        List<TeamSnapshotResult> teams,
        List<TeamChangeHistoryResult> histories
) {
}