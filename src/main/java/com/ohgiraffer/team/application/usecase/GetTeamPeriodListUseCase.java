package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetTeamPeriodListUseCase {

    List<TeamPeriodResult> getTeamPeriods(
            Long requesterId,
            Role requesterRole
    );
}