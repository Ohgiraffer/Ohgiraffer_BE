package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.command.AssignTeamMemberCommand;
import com.ohgiraffer.team.application.command.CreateTeamCommand;
import com.ohgiraffer.team.application.command.MoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.RemoveTeamMemberCommand;
import com.ohgiraffer.team.application.command.SaveTeamAssignmentsCommand;
import com.ohgiraffer.team.application.command.TeamAssignmentCommand;
import com.ohgiraffer.team.application.command.UpdateTeamCommand;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberResult;
import com.ohgiraffer.team.application.usecase.AssignTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.CreateTeamResult;
import com.ohgiraffer.team.application.usecase.CreateTeamUseCase;
import com.ohgiraffer.team.application.usecase.MoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.RemoveTeamMemberUseCase;
import com.ohgiraffer.team.application.usecase.SaveTeamAssignmentsUseCase;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamCommandService
        implements CreateTeamUseCase,
        UpdateTeamUseCase,
        AssignTeamMemberUseCase,
        MoveTeamMemberUseCase,
        RemoveTeamMemberUseCase,
        SaveTeamAssignmentsUseCase {

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

    @Override
    public void saveTeamAssignments(
            SaveTeamAssignmentsCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateAssignmentRequest(
                command
        );

        Map<Long, Team> teamMap =
                teamRepository.findAll()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Team::getId,
                                        Function.identity()
                                )
                        );

        validateRequestedTeams(
                command.teams(),
                teamMap
        );

        Map<Long, Long> desiredTeamByUserId =
                createDesiredTeamByUserId(
                        command.teams()
                );

        Set<Long> unassignedUserIds =
                new HashSet<>(
                        command.unassignedUserIds()
                );

        Set<Long> requestedUserIds =
                new HashSet<>();

        requestedUserIds.addAll(
                desiredTeamByUserId.keySet()
        );

        requestedUserIds.addAll(
                unassignedUserIds
        );

        validateAssignableUsers(
                requestedUserIds
        );

        List<TeamMember> activeMembers =
                teamRepository.findActiveMembersForUpdate();

        Map<Long, TeamMember> activeMemberByUserId =
                createActiveMemberByUserId(
                        activeMembers
                );

        LocalDateTime changedAt =
                LocalDateTime.now();

        desiredTeamByUserId.forEach((userId, targetTeamId) ->
                applyAssignedState(
                        userId,
                        targetTeamId,
                        activeMemberByUserId.get(userId),
                        changedAt
                )
        );

        unassignedUserIds.forEach(userId ->
                applyUnassignedState(
                        activeMemberByUserId.get(userId),
                        changedAt
                )
        );
    }

    private void applyAssignedState(
            Long userId,
            Long targetTeamId,
            TeamMember currentMember,
            LocalDateTime changedAt
    ) {
        if (currentMember == null) {
            teamRepository.saveMember(
                    TeamMember.create(
                            targetTeamId,
                            userId
                    )
            );
            return;
        }

        if (currentMember.getTeamId()
                .equals(targetTeamId)) {
            return;
        }

        teamRepository.saveMember(
                currentMember.leave(
                        changedAt
                )
        );

        teamRepository.saveMember(
                TeamMember.create(
                        targetTeamId,
                        userId
                )
        );
    }

    private void applyUnassignedState(
            TeamMember currentMember,
            LocalDateTime changedAt
    ) {
        if (currentMember == null) {
            return;
        }

        teamRepository.saveMember(
                currentMember.leave(
                        changedAt
                )
        );
    }

    private Map<Long, TeamMember> createActiveMemberByUserId(
            List<TeamMember> activeMembers
    ) {
        Map<Long, TeamMember> activeMemberByUserId =
                new LinkedHashMap<>();

        for (TeamMember activeMember : activeMembers) {
            TeamMember duplicatedMember =
                    activeMemberByUserId.putIfAbsent(
                            activeMember.getUserId(),
                            activeMember
                    );

            if (duplicatedMember != null) {
                throw new BusinessException(
                        ErrorCode.TEAM_MEMBER_ALREADY_ASSIGNED
                );
            }
        }

        return activeMemberByUserId;
    }

    private Map<Long, Long> createDesiredTeamByUserId(
            List<TeamAssignmentCommand> assignments
    ) {
        return assignments.stream()
                .flatMap(assignment ->
                        assignment.userIds()
                                .stream()
                                .map(userId ->
                                        Map.entry(
                                                userId,
                                                assignment.teamId()
                                        )
                                )
                )
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                );
    }

    private void validateAssignmentRequest(
            SaveTeamAssignmentsCommand command
    ) {
        Set<Long> teamIds =
                new HashSet<>();

        for (TeamAssignmentCommand assignment : command.teams()) {
            validateTeamId(
                    assignment.teamId()
            );

            if (!teamIds.add(
                    assignment.teamId()
            )) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "중복된 팀이 포함되어 있습니다."
                );
            }

            assignment.userIds()
                    .forEach(this::validateUserId);
        }

        command.unassignedUserIds()
                .forEach(this::validateUserId);

        validateDuplicateAssignmentUsers(
                command
        );
    }

    private void validateDuplicateAssignmentUsers(
            SaveTeamAssignmentsCommand command
    ) {
        Set<Long> userIds =
                new HashSet<>();

        for (TeamAssignmentCommand assignment : command.teams()) {
            for (Long userId : assignment.userIds()) {
                if (!userIds.add(userId)) {
                    throw new BusinessException(
                            ErrorCode.INVALID_INPUT_VALUE,
                            "중복된 훈련생이 포함되어 있습니다."
                    );
                }
            }
        }

        for (Long userId : command.unassignedUserIds()) {
            if (!userIds.add(userId)) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "중복된 훈련생이 포함되어 있습니다."
                );
            }
        }
    }

    private void validateRequestedTeams(
            List<TeamAssignmentCommand> assignments,
            Map<Long, Team> teamMap
    ) {
        for (TeamAssignmentCommand assignment : assignments) {
            Team team =
                    teamMap.get(
                            assignment.teamId()
                    );

            if (team == null) {
                throw new BusinessException(
                        ErrorCode.TEAM_NOT_FOUND
                );
            }

            validateTeamAssignable(
                    team
            );
        }
    }

    private void validateAssignableUsers(
            Set<Long> userIds
    ) {
        if (userIds.isEmpty()) {
            return;
        }

        List<User> users =
                userRepository.findByIdIn(
                        userIds.stream()
                                .toList()
                );

        if (users.size() != userIds.size()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        users.forEach(
                this::validateAssignableStudent
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