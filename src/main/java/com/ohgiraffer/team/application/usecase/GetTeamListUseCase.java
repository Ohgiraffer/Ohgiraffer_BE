package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetTeamListUseCase {

    List<TeamListResult> getTeams(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId
    );
}