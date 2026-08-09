package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.TeamMemberResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService
        implements GetTeamListUseCase,
        GetUnassignedStudentUseCase {

    private final TeamRepository teamRepository;

    @Override
    public List<TeamListResult> getTeams(
            Long requesterId,
            Role requesterRole
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        List<Team> teams =
                teamRepository.findVisibleTeams();

        List<Long> teamIds =
                teams.stream()
                        .map(Team::getId)
                        .toList();

        Map<Long, List<TeamMemberResult>> memberMap =
                teamRepository.findActiveMembersByTeamIds(teamIds)
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamMember::getTeamId,
                                        Collectors.mapping(
                                                TeamMemberResult::from,
                                                Collectors.toList()
                                        )
                                )
                        );

        return teams.stream()
                .map(team ->
                        TeamListResult.of(
                                team,
                                memberMap.getOrDefault(
                                        team.getId(),
                                        List.of()
                                )
                        )
                )
                .toList();
    }

    @Override
    public List<UnassignedStudentResult> getUnassignedStudents(
            Long requesterId,
            Role requesterRole
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateManagerAccess(
                requesterRole
        );

        return teamRepository.findUnassignedStudents()
                .stream()
                .map(UnassignedStudentResult::from)
                .toList();
    }

    private void validateRequester(
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterId == null
                || requesterId <= 0
                || requesterRole == null) {
            throw new BusinessException(
                    ErrorCode.TEAM_ACCESS_DENIED
            );
        }
    }

    private void validateManagerAccess(
            Role requesterRole
    ) {
        if (requesterRole != Role.INSTRUCTOR
                && requesterRole != Role.MANAGER) {
            throw new BusinessException(
                    ErrorCode.TEAM_ACCESS_DENIED
            );
        }
    }
}