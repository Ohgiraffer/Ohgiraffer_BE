package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.global.s3.S3UrlResolver;
import com.ohgiraffer.team.application.usecase.GetTeamListUseCase;
import com.ohgiraffer.team.application.usecase.GetTeamPeriodListUseCase;
import com.ohgiraffer.team.application.usecase.GetUnassignedStudentUseCase;
import com.ohgiraffer.team.application.usecase.GetUserTeamHistoryUseCase;
import com.ohgiraffer.team.application.usecase.TeamListResult;
import com.ohgiraffer.team.application.usecase.TeamMemberResult;
import com.ohgiraffer.team.application.usecase.TeamPeriodResult;
import com.ohgiraffer.team.application.usecase.UnassignedStudentResult;
import com.ohgiraffer.team.application.usecase.UserTeamHistoryResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.UnassignedStudent;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
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
        GetUnassignedStudentUseCase,
        GetTeamPeriodListUseCase,
        GetUserTeamHistoryUseCase {

    private final TeamRepository teamRepository;
    private final TeamPeriodRepository teamPeriodRepository;
    private final S3UrlResolver s3UrlResolver;

    @Override
    public List<TeamListResult> getTeams(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateAndGetTeamPeriod(
                teamPeriodId
        );

        List<Team> teams =
                teamRepository.findVisibleTeamsByPeriodId(
                        teamPeriodId
                );

        List<Long> teamIds =
                teams.stream()
                        .map(Team::getId)
                        .toList();

        Map<Long, List<TeamMemberResult>> memberMap =
                teamRepository.findActiveMembersByTeamIds(
                                teamIds
                        )
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamMember::getTeamId,
                                        Collectors.mapping(
                                                this::toTeamMemberResult,
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
    public List<TeamPeriodResult> getTeamPeriods(
            Long requesterId,
            Role requesterRole
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        return teamPeriodRepository.findVisiblePeriods()
                .stream()
                .map(TeamPeriodResult::from)
                .toList();
    }

    @Override
    public List<UnassignedStudentResult> getUnassignedStudents(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateManagerAccess(
                requesterRole
        );

        validateAndGetTeamPeriod(
                teamPeriodId
        );

        return teamRepository.findUnassignedStudents(
                        teamPeriodId
                )
                .stream()
                .map(this::toUnassignedStudentResult)
                .toList();
    }

    @Override
    public List<UserTeamHistoryResult> getUserTeamHistories(
            Long requesterId,
            Role requesterRole,
            Long userId
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateManagerAccess(
                requesterRole
        );

        validateUserId(
                userId
        );

        return teamRepository.findUserTeamHistories(
                userId
        );
    }

    private TeamMemberResult toTeamMemberResult(
            TeamMember member
    ) {
        return new TeamMemberResult(
                member.getId(),
                member.getUserId(),
                member.getUserName(),
                member.getEmail(),
                resolveProfileImgUrl(
                        member.getProfileImg()
                ),
                member.getJoinedAt()
        );
    }

    private UnassignedStudentResult toUnassignedStudentResult(
            UnassignedStudent student
    ) {
        return new UnassignedStudentResult(
                student.getUserId(),
                student.getName(),
                student.getEmail(),
                resolveProfileImgUrl(
                        student.getProfileImg()
                )
        );
    }

    private String resolveProfileImgUrl(
            String profileImg
    ) {
        if (profileImg == null
                || profileImg.isBlank()) {
            return null;
        }

        return s3UrlResolver.resolve(
                profileImg
        );
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

    private void validateAndGetTeamPeriod(
            Long teamPeriodId
    ) {
        validateTeamPeriodId(
                teamPeriodId
        );

        teamPeriodRepository.findById(
                        teamPeriodId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TEAM_NOT_FOUND
                        )
                );
    }

    private void validateTeamPeriodId(
            Long teamPeriodId
    ) {
        if (teamPeriodId == null
                || teamPeriodId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀 기간 ID가 올바르지 않습니다."
            );
        }
    }

    private void validateUserId(
            Long userId
    ) {
        if (userId == null
                || userId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "사용자 ID가 올바르지 않습니다."
            );
        }
    }
}