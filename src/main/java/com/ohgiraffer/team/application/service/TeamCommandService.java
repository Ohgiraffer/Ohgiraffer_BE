package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.command.CreateTeamPeriodCommand;
import com.ohgiraffer.team.application.command.SaveTeamConfigurationCommand;
import com.ohgiraffer.team.application.command.TeamConfigurationCommand;
import com.ohgiraffer.team.application.event.TeamChannelSyncTarget;
import com.ohgiraffer.team.application.event.TeamConfigurationSavedEvent;
import com.ohgiraffer.team.application.usecase.CreateTeamPeriodUseCase;
import com.ohgiraffer.team.application.usecase.SaveTeamConfigurationUseCase;
import com.ohgiraffer.team.application.usecase.TeamPeriodResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMember;
import com.ohgiraffer.team.domain.model.TeamPeriod;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.UserStatus;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        implements CreateTeamPeriodUseCase,
        SaveTeamConfigurationUseCase {

    private final TeamRepository teamRepository;
    private final TeamPeriodRepository teamPeriodRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TeamPeriodResult createTeamPeriod(
            CreateTeamPeriodCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        TeamPeriod teamPeriod =
                TeamPeriod.create(
                        command.startDate(),
                        command.endDate()
                );

        TeamPeriod savedTeamPeriod =
                teamPeriodRepository.save(
                        teamPeriod
                );

        return TeamPeriodResult.from(
                savedTeamPeriod
        );
    }

    @Override
    public void saveTeamConfiguration(
            SaveTeamConfigurationCommand command,
            Role requesterRole
    ) {
        validateManagerAccess(
                command.requesterId(),
                requesterRole
        );

        validateConfigurationRequest(
                command
        );

        TeamPeriod teamPeriod =
                teamPeriodRepository.findByIdForUpdate(
                                command.teamPeriodId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TEAM_NOT_FOUND
                                )
                        );

        teamPeriod.validateAssignable();

        LocalDateTime changedAt =
                LocalDateTime.now();

        Map<Long, Team> visibleTeamById =
                teamRepository.findVisibleTeamsByPeriodId(
                                teamPeriod.getId()
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Team::getId,
                                        Function.identity()
                                )
                        );

        validateRequestedTeams(
                command.teams(),
                visibleTeamById,
                teamPeriod.getId()
        );

        validateDeletedTeams(
                command.deletedTeamIds(),
                visibleTeamById
        );

        List<Team> savedTeams =
                saveRequestedTeams(
                        command.teams(),
                        visibleTeamById,
                        teamPeriod
                );

        Map<Long, Long> desiredTeamByUserId =
                createDesiredTeamByUserId(
                        savedTeams,
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

        Set<Long> affectedTeamIds =
                createAffectedTeamIds(
                        savedTeams,
                        command.deletedTeamIds(),
                        requestedUserIds,
                        activeMembers
                );

        closeDeletedTeamMembers(
                command.deletedTeamIds(),
                requestedUserIds,
                activeMembers,
                changedAt
        );

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

        markDeletedTeams(
                command.deletedTeamIds(),
                visibleTeamById,
                changedAt
        );

        publishTeamConfigurationSavedEvent(
                affectedTeamIds,
                command.deletedTeamIds(),
                command.createChatChannel()
        );
    }

    private List<Team> saveRequestedTeams(
            List<TeamConfigurationCommand> teamCommands,
            Map<Long, Team> visibleTeamById,
            TeamPeriod teamPeriod
    ) {
        return teamCommands.stream()
                .map(command -> saveRequestedTeam(
                        command,
                        visibleTeamById,
                        teamPeriod
                ))
                .toList();
    }

    private Team saveRequestedTeam(
            TeamConfigurationCommand command,
            Map<Long, Team> visibleTeamById,
            TeamPeriod teamPeriod
    ) {
        if (command.teamId() == null) {
            Team team =
                    Team.create(
                            teamPeriod.getId(),
                            command.name(),
                            teamPeriod.getStartDate(),
                            teamPeriod.getEndDate()
                    );

            validateDuplicateName(
                    team.getName(),
                    teamPeriod.getId()
            );

            return teamRepository.save(
                    team
            );
        }

        Team team =
                visibleTeamById.get(
                        command.teamId()
                );

        if (team == null) {
            throw new BusinessException(
                    ErrorCode.TEAM_NOT_FOUND
            );
        }

        Team updatedTeam =
                team.update(
                        command.name(),
                        teamPeriod.getStartDate(),
                        teamPeriod.getEndDate()
                );

        validateDuplicateNameForUpdate(
                updatedTeam.getName(),
                teamPeriod.getId(),
                updatedTeam.getId()
        );

        return teamRepository.save(
                updatedTeam
        );
    }

    private Map<Long, Long> createDesiredTeamByUserId(
            List<Team> savedTeams,
            List<TeamConfigurationCommand> teamCommands
    ) {
        Map<Long, Long> desiredTeamByUserId =
                new LinkedHashMap<>();

        for (int index = 0; index < teamCommands.size(); index++) {
            Team savedTeam =
                    savedTeams.get(
                            index
                    );

            TeamConfigurationCommand command =
                    teamCommands.get(
                            index
                    );

            for (Long userId : command.userIds()) {
                Long previousTeamId =
                        desiredTeamByUserId.putIfAbsent(
                                userId,
                                savedTeam.getId()
                        );

                if (previousTeamId != null) {
                    throw new BusinessException(
                            ErrorCode.INVALID_INPUT_VALUE,
                            "중복된 훈련생이 포함되어 있습니다."
                    );
                }
            }
        }

        return desiredTeamByUserId;
    }

    private Set<Long> createAffectedTeamIds(
            List<Team> savedTeams,
            List<Long> deletedTeamIds,
            Set<Long> requestedUserIds,
            List<TeamMember> activeMembers
    ) {
        Set<Long> affectedTeamIds =
                new HashSet<>();

        savedTeams.stream()
                .map(Team::getId)
                .forEach(affectedTeamIds::add);

        affectedTeamIds.addAll(
                deletedTeamIds
        );

        activeMembers.stream()
                .filter(member -> requestedUserIds.contains(
                        member.getUserId()
                ))
                .map(TeamMember::getTeamId)
                .forEach(affectedTeamIds::add);

        return affectedTeamIds;
    }

    private void closeDeletedTeamMembers(
            List<Long> deletedTeamIds,
            Set<Long> requestedUserIds,
            List<TeamMember> activeMembers,
            LocalDateTime changedAt
    ) {
        Set<Long> deletedTeamIdSet =
                new HashSet<>(
                        deletedTeamIds
                );

        activeMembers.stream()
                .filter(member -> deletedTeamIdSet.contains(
                        member.getTeamId()
                ))
                .filter(member -> !requestedUserIds.contains(
                        member.getUserId()
                ))
                .map(member -> member.leave(
                        changedAt
                ))
                .forEach(teamRepository::saveMember);
    }

    private void markDeletedTeams(
            List<Long> deletedTeamIds,
            Map<Long, Team> visibleTeamById,
            LocalDateTime deletedAt
    ) {
        deletedTeamIds.stream()
                .map(visibleTeamById::get)
                .map(team -> team.markDeleted(
                        deletedAt
                ))
                .forEach(teamRepository::save);
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

    private void publishTeamConfigurationSavedEvent(
            Set<Long> affectedTeamIds,
            List<Long> deletedTeamIds,
            boolean createChatChannel
    ) {
        if (affectedTeamIds.isEmpty()) {
            return;
        }

        Set<Long> deletedTeamIdSet =
                new HashSet<>(
                        deletedTeamIds
                );

        Map<Long, List<Long>> activeUserIdsByTeamId =
                teamRepository.findActiveMembersByTeamIds(
                                affectedTeamIds.stream()
                                        .filter(teamId -> !deletedTeamIdSet.contains(
                                                teamId
                                        ))
                                        .toList()
                        )
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamMember::getTeamId,
                                        Collectors.mapping(
                                                TeamMember::getUserId,
                                                Collectors.toList()
                                        )
                                )
                        );

        List<TeamChannelSyncTarget> channelSyncTargets =
                new ArrayList<>();

        affectedTeamIds.forEach(teamId -> {
            List<Long> memberUserIds =
                    deletedTeamIdSet.contains(
                            teamId
                    )
                            ? List.of()
                            : activeUserIdsByTeamId.getOrDefault(
                            teamId,
                            List.of()
                    );

            channelSyncTargets.add(
                    new TeamChannelSyncTarget(
                            teamId,
                            memberUserIds
                    )
            );
        });

        eventPublisher.publishEvent(
                new TeamConfigurationSavedEvent(
                        channelSyncTargets,
                        createChatChannel
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

    private void validateConfigurationRequest(
            SaveTeamConfigurationCommand command
    ) {
        validateTeamPeriodId(
                command.teamPeriodId()
        );

        validateTeams(
                command.teams()
        );

        validateDeletedTeamIds(
                command.deletedTeamIds()
        );

        command.unassignedUserIds()
                .forEach(this::validateUserId);

        validateDuplicateUsersAcrossRequest(
                command
        );
    }

    private void validateTeams(
            List<TeamConfigurationCommand> teams
    ) {
        Set<Long> teamIds =
                new HashSet<>();

        Set<String> teamNames =
                new HashSet<>();

        for (TeamConfigurationCommand team : teams) {
            if (team.teamId() != null) {
                validateTeamId(
                        team.teamId()
                );

                if (!teamIds.add(team.teamId())) {
                    throw new BusinessException(
                            ErrorCode.INVALID_INPUT_VALUE,
                            "중복된 팀이 포함되어 있습니다."
                    );
                }
            }

            if (team.name() != null
                    && !teamNames.add(team.name().trim())) {
                throw new BusinessException(
                        ErrorCode.TEAM_DUPLICATE_NAME
                );
            }

            team.userIds()
                    .forEach(this::validateUserId);
        }
    }

    private void validateRequestedTeams(
            List<TeamConfigurationCommand> teams,
            Map<Long, Team> visibleTeamById,
            Long teamPeriodId
    ) {
        for (TeamConfigurationCommand team : teams) {
            if (team.teamId() == null) {
                continue;
            }

            Team existingTeam =
                    visibleTeamById.get(
                            team.teamId()
                    );

            if (existingTeam == null
                    || !existingTeam.getTeamPeriodId()
                    .equals(teamPeriodId)) {
                throw new BusinessException(
                        ErrorCode.TEAM_NOT_FOUND
                );
            }
        }
    }

    private void validateDeletedTeamIds(
            List<Long> deletedTeamIds
    ) {
        Set<Long> teamIds =
                new HashSet<>();

        for (Long teamId : deletedTeamIds) {
            validateTeamId(
                    teamId
            );

            if (!teamIds.add(teamId)) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "중복된 삭제 팀이 포함되어 있습니다."
                );
            }
        }
    }

    private void validateDeletedTeams(
            List<Long> deletedTeamIds,
            Map<Long, Team> visibleTeamById
    ) {
        for (Long teamId : deletedTeamIds) {
            if (!visibleTeamById.containsKey(teamId)) {
                throw new BusinessException(
                        ErrorCode.TEAM_NOT_FOUND
                );
            }
        }
    }

    private void validateDuplicateUsersAcrossRequest(
            SaveTeamConfigurationCommand command
    ) {
        Set<Long> userIds =
                new HashSet<>();

        for (TeamConfigurationCommand team : command.teams()) {
            for (Long userId : team.userIds()) {
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

    private void validateDuplicateName(
            String name,
            Long teamPeriodId
    ) {
        if (teamRepository.existsByNameAndTeamPeriodId(
                name,
                teamPeriodId
        )) {
            throw new BusinessException(
                    ErrorCode.TEAM_DUPLICATE_NAME
            );
        }
    }

    private void validateDuplicateNameForUpdate(
            String name,
            Long teamPeriodId,
            Long teamId
    ) {
        if (teamRepository.existsByNameAndTeamPeriodIdAndIdNot(
                name,
                teamPeriodId,
                teamId
        )) {
            throw new BusinessException(
                    ErrorCode.TEAM_DUPLICATE_NAME
            );
        }
    }
}