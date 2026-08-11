package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamNotionPageUpdater {

    private final TeamRepository teamRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateNotionPageId(
            Long teamId,
            String notionPageId
    ) {
        Team team =
                teamRepository.findByIdForUpdate(teamId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));

        Team updatedTeam =
                team.assignNotionPageId(notionPageId);

        teamRepository.save(updatedTeam);
    }
}