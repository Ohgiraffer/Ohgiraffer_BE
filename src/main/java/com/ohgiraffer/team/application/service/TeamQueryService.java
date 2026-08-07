package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.usecase.GetTeamDetailUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.TeamDetailResult;
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
        GetTeamDetailUseCase,
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
                teamRepository.findAll();

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
    public TeamDetailResult getTeam(
            Long teamId,
            Long requesterId,
            Role requesterRole
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateTeamId(
                teamId
        );

        Team team =
                teamRepository.findById(teamId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        validateTeamDetailAccess(
                teamId,
                requesterId,
                requesterRole
        );

        List<TeamMemberResult> members =
                teamRepository.findActiveMembersByTeamId(teamId)
                        .stream()
                        .map(TeamMemberResult::from)
                        .toList();

        return TeamDetailResult.of(
                team,
                members
        );
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

    private void validateTeamDetailAccess(
            Long teamId,
            Long requesterId,
            Role requesterRole
    ) {
        if (requesterRole == Role.INSTRUCTOR
                || requesterRole == Role.MANAGER) {
            return;
        }

        boolean activeMember =
                teamRepository.existsActiveMember(
                        teamId,
                        requesterId
                );

        if (!activeMember) {
            throw new BusinessException(
                    ErrorCode.TEAM_ACCESS_DENIED
            );
        }
    }

    private void validateTeamId(
            Long teamId
    ) {
        if (teamId == null
                || teamId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 ID가 올바르지 않습니다."
            );
        }
    }
}