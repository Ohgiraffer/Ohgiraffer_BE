package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.command.AssignTeamMemberCommand;
import com.ohgiraffer.team.application.command.CreateTeamCommand;
import com.ohgiraffer.team.application.command.MoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.RemoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.UpdateTeamCommand;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberResult;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.CreateTeamResult;
import com.ohgiraffer.team.application.usecase.CreateTeamUseCase;
import com.ohgiraffer.team.application.usecase.MoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.RemoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.TeamDetailResult;
import com.ohgiraffer.team.application.usecase.TeamMemberResult;
import com.ohgiraffer.team.application.usecase.UpdateTeamUseCase;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamCommandService
        implements CreateTeamUseCase,
        UpdateTeamUseCase,
        AssignTeamMemberUseCase,
        MoveTeamMemberUseCase,
        RemoveTeamMemberUseCase {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public CreateTeamResult createTeam(
            CreateTeamCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        Team team =
                Team.create(
                        command.name(),
                        command.startDate(),
                        command.endDate()
                );

        validateDuplicateName(
                team.getName()
        );

        Team savedTeam =
                teamRepository.save(
                        team
                );

        return CreateTeamResult.from(
                savedTeam
        );
    }

    @Override
    public TeamDetailResult updateTeam(
            UpdateTeamCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamId(
                command.teamId()
        );

        Team team =
                teamRepository.findByIdForUpdate(
                                command.teamId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        Team updatedTeam =
                team.update(
                        command.name(),
                        command.startDate(),
                        command.endDate()
                );

        validateDuplicateNameForUpdate(
                updatedTeam.getName(),
                updatedTeam.getId()
        );

        Team savedTeam =
                teamRepository.save(
                        updatedTeam
                );

        List<TeamMemberResult> members =
                teamRepository.findActiveMembersByTeamId(
                                savedTeam.getId()
                        )
                        .stream()
                        .map(TeamMemberResult::from)
                        .toList();

        return TeamDetailResult.of(
                savedTeam,
                members
        );
    }

    @Override
    public AssignTeamMemberResult assignTeamMember(
            AssignTeamMemberCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamId(
                command.teamId()
        );

        validateUserId(
                command.userId()
        );

        Team team =
                teamRepository.findById(
                                command.teamId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        validateTeamAssignable(
                team
        );

        User user =
                findAssignableUser(
                        command.userId()
                );

        validateNotAssigned(
                user.getId()
        );

        TeamMember teamMember =
                TeamMember.create(
                        team.getId(),
                        user.getId()
                );

        TeamMember savedMember =
                teamRepository.saveMember(
                        teamMember
                );

        return AssignTeamMemberResult.of(
                savedMember,
                user
        );
    }

    @Override
    public AssignTeamMemberResult moveTeamMember(
            MoveTeamMemberCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamId(
                command.sourceTeamId()
        );

        validateTeamId(
                command.targetTeamId()
        );

        validateTeamMemberId(
                command.teamMemberId()
        );

        if (command.sourceTeamId()
                .equals(command.targetTeamId())) {
            throw new BusinessException(
                    ErrorCode.TEAM_SAME_TARGET
            );
        }

        Team sourceTeam =
                teamRepository.findById(
                                command.sourceTeamId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        Team targetTeam =
                teamRepository.findById(
                                command.targetTeamId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        validateTeamAssignable(
                sourceTeam
        );

        validateTeamAssignable(
                targetTeam
        );

        TeamMember currentMember =
                teamRepository.findMemberByIdForUpdate(
                                command.teamMemberId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_MEMBER_NOT_FOUND
                                )
                        );

        currentMember.validateBelongsTo(
                sourceTeam.getId()
        );

        TeamMember closedMember =
                currentMember.leave(
                        LocalDateTime.now()
                );

        teamRepository.saveMember(
                closedMember
        );

        User user =
                findAssignableUser(
                        currentMember.getUserId()
                );

        TeamMember newMember =
                TeamMember.create(
                        targetTeam.getId(),
                        user.getId()
                );

        TeamMember savedMember =
                teamRepository.saveMember(
                        newMember
                );

        return AssignTeamMemberResult.of(
                savedMember,
                user
        );
    }

    @Override
    public void removeTeamMember(
            RemoveTeamMemberCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateTeamId(
                command.teamId()
        );

        validateTeamMemberId(
                command.teamMemberId()
        );

        Team team =
                teamRepository.findById(
                                command.teamId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        validateTeamAssignable(
                team
        );

        TeamMember currentMember =
                teamRepository.findMemberByIdForUpdate(
                                command.teamMemberId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_MEMBER_NOT_FOUND
                                )
                        );

        currentMember.validateBelongsTo(
                team.getId()
        );

        TeamMember removedMember =
                currentMember.leave(
                        LocalDateTime.now()
                );

        teamRepository.saveMember(
                removedMember
        );
    }

    private User findAssignableUser(
            Long userId
    ) {
        User user =
                userRepository.findById(
                                userId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                        );

        validateAssignableStudent(
                user
        );

        return user;
    }

    private void validateManagerAccess(
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

        if (requesterRole != Role.INSTRUCTOR
                && requesterRole != Role.MANAGER) {
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

    private void validateTeamMemberId(
            Long teamMemberId
    ) {
        if (teamMemberId == null
                || teamMemberId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "팀원 배정 ID가 올바르지 않습니다."
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

    private void validateTeamAssignable(
            Team team
    ) {
        if (team.isDissolved()) {
            throw new BusinessException(
                    ErrorCode.TEAM_ALREADY_DISSOLVED
            );
        }
    }

    private void validateAssignableStudent(
            User user
    ) {
        if (user.getRole() != Role.STUDENT
                || user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.TEAM_MEMBER_INVALID_USER
            );
        }
    }

    private void validateNotAssigned(
            Long userId
    ) {
        if (teamRepository.existsActiveMemberByUserId(userId)) {
            throw new BusinessException(
                    ErrorCode.TEAM_MEMBER_ALREADY_ASSIGNED
            );
        }
    }

    private void validateDuplicateName(
            String name
    ) {
        if (teamRepository.existsByName(name)) {
            throw new BusinessException(
                    ErrorCode.TEAM_DUPLICATE_NAME
            );
        }
    }

    private void validateDuplicateNameForUpdate(
            String name,
            Long teamId
    ) {
        if (teamRepository.existsByNameAndIdNot(
                name,
                teamId
        )) {
            throw new BusinessException(
                    ErrorCode.TEAM_DUPLICATE_NAME
            );
        }
    }
}