package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.time.LocalDate;

public interface GetTeamHistoryUseCase {

    TeamHistoryResult getTeamHistories(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId,
            LocalDate startDate,
            LocalDate endDate
    );
}