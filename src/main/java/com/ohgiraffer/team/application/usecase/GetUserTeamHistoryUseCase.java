package com.ohgiraffer.team.application.usecase;

import com.ohgiraffer.user.domain.model.Role;

import java.util.List;

public interface GetUserTeamHistoryUseCase {

    List<UserTeamHistoryResult> getUserTeamHistories(
            Long requesterId,
            Role requesterRole,
            Long userId
    );
}