package com.ohgiraffer.team.application.service;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.team.application.usecase.GetTeamHistoryUseCase;
import com.ohgiraffer.team.application.usecase.TeamChangeHistoryResult;
import com.ohgiraffer.team.application.usecase.TeamHistoryResult;
import com.ohgiraffer.team.application.usecase.TeamSnapshotMemberResult;
import com.ohgiraffer.team.application.usecase.TeamSnapshotResult;
import com.ohgiraffer.team.domain.model.Team;
import com.ohgiraffer.team.domain.model.TeamMemberHistory;
import com.ohgiraffer.team.domain.model.TeamSnapshotMember;
import com.ohgiraffer.team.domain.repository.TeamHistoryRepository;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
import com.ohgiraffer.team.domain.repository.TeamRepository;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamHistoryQueryService
        implements GetTeamHistoryUseCase {

    private static final String UNASSIGNED_TEAM_NAME = "미배정";
    private static final long MOVE_EVENT_THRESHOLD_MINUTES = 1L;

    private final TeamRepository teamRepository;
    private final TeamHistoryRepository teamHistoryRepository;
    private final TeamPeriodRepository teamPeriodRepository;

    @Override
    public TeamHistoryResult getTeamHistories(
            Long requesterId,
            Role requesterRole,
            Long teamPeriodId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateRequester(
                requesterId,
                requesterRole
        );

        validateTeamPeriodId(
                teamPeriodId
        );

        validatePeriod(
                startDate,
                endDate
        );

        teamPeriodRepository.findById(
                        teamPeriodId
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.TEAM_NOT_FOUND
                        )
                );

        LocalDateTime startAt =
                startDate.atStartOfDay();

        LocalDateTime endAt =
                endDate.atTime(
                        LocalTime.MAX
                );

        List<Team> teams =
                teamRepository.findVisibleTeamsByPeriodId(
                        teamPeriodId
                );

        List<TeamSnapshotMember> snapshotMembers =
                teamHistoryRepository.findSnapshotMembers(
                        teamPeriodId,
                        endAt
                );

        List<TeamSnapshotResult> teamSnapshots =
                createTeamSnapshots(
                        teams,
                        snapshotMembers
                );

        List<TeamMemberHistory> memberHistories =
                teamHistoryRepository.findHistoriesIntersectingPeriod(
                        teamPeriodId,
                        startAt,
                        endAt
                );

        List<TeamChangeHistoryResult> histories =
                createChangeHistories(
                        memberHistories,
                        startAt,
                        endAt
                );

        return new TeamHistoryResult(
                endDate,
                teamSnapshots,
                histories
        );
    }

    private List<TeamSnapshotResult> createTeamSnapshots(
            List<Team> teams,
            List<TeamSnapshotMember> snapshotMembers
    ) {
        Map<Long, List<TeamSnapshotMemberResult>> memberMap =
                snapshotMembers.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamSnapshotMember::getTeamId,
                                        Collectors.mapping(
                                                member -> new TeamSnapshotMemberResult(
                                                        member.getUserId(),
                                                        member.getUserName()
                                                ),
                                                Collectors.toList()
                                        )
                                )
                        );

        return teams.stream()
                .map(team -> {
                    List<TeamSnapshotMemberResult> members =
                            memberMap.getOrDefault(
                                    team.getId(),
                                    List.of()
                            );

                    return new TeamSnapshotResult(
                            team.getId(),
                            team.getName(),
                            members.size(),
                            members
                    );
                })
                .toList();
    }

    private List<TeamChangeHistoryResult> createChangeHistories(
            List<TeamMemberHistory> memberHistories,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Map<Long, List<TeamMemberHistory>> historiesByUser =
                memberHistories.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TeamMemberHistory::getUserId
                                )
                        );

        return historiesByUser.values()
                .stream()
                .flatMap(histories -> createUserChangeHistories(
                        histories,
                        startAt,
                        endAt
                ).stream())
                .sorted(
                        Comparator.comparing(TeamChangeHistoryResult::changedAt)
                                .thenComparing(TeamChangeHistoryResult::userName)
                                .thenComparing(TeamChangeHistoryResult::userId)
                )
                .toList();
    }

    private List<TeamChangeHistoryResult> createUserChangeHistories(
            List<TeamMemberHistory> histories,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        List<TeamMemberHistory> sortedHistories =
                histories.stream()
                        .sorted(
                                Comparator.comparing(TeamMemberHistory::getJoinedAt)
                                        .thenComparing(TeamMemberHistory::getTeamMemberId)
                        )
                        .toList();

        List<TeamChangeHistoryResult> joinedEvents =
                sortedHistories.stream()
                        .filter(history -> isBetween(
                                history.getJoinedAt(),
                                startAt,
                                endAt
                        ))
                        .map(history -> createJoinedEvent(
                                history,
                                findPreviousHistory(
                                        sortedHistories,
                                        history
                                )
                        ))
                        .toList();

        List<TeamChangeHistoryResult> leftEvents =
                sortedHistories.stream()
                        .filter(history -> history.getLeftAt() != null)
                        .filter(history -> isBetween(
                                history.getLeftAt(),
                                startAt,
                                endAt
                        ))
                        .filter(history -> !hasNextJoinedEventInPeriod(
                                sortedHistories,
                                history,
                                startAt,
                                endAt
                        ))
                        .map(this::createLeftEvent)
                        .toList();

        return java.util.stream.Stream.concat(
                        joinedEvents.stream(),
                        leftEvents.stream()
                )
                .toList();
    }

    private TeamChangeHistoryResult createJoinedEvent(
            TeamMemberHistory current,
            TeamMemberHistory previous
    ) {
        Long fromTeamId =
                previous == null ? null : previous.getTeamId();

        String fromTeamName =
                previous == null ? UNASSIGNED_TEAM_NAME : previous.getTeamName();

        return new TeamChangeHistoryResult(
                current.getUserId(),
                current.getUserName(),
                fromTeamId,
                fromTeamName,
                current.getTeamId(),
                current.getTeamName(),
                current.getJoinedAt()
        );
    }

    private TeamChangeHistoryResult createLeftEvent(
            TeamMemberHistory current
    ) {
        return new TeamChangeHistoryResult(
                current.getUserId(),
                current.getUserName(),
                current.getTeamId(),
                current.getTeamName(),
                null,
                UNASSIGNED_TEAM_NAME,
                current.getLeftAt()
        );
    }

    private TeamMemberHistory findPreviousHistory(
            List<TeamMemberHistory> histories,
            TeamMemberHistory current
    ) {
        return histories.stream()
                .filter(history -> !history.getTeamMemberId()
                        .equals(
                                current.getTeamMemberId()
                        ))
                .filter(history -> history.getLeftAt() != null)
                .filter(history -> !history.getLeftAt()
                        .isAfter(
                                current.getJoinedAt()
                        ))
                .filter(history -> isMoveEvent(
                        history.getLeftAt(),
                        current.getJoinedAt()
                ))
                .max(
                        Comparator.comparing(TeamMemberHistory::getLeftAt)
                                .thenComparing(TeamMemberHistory::getTeamMemberId)
                )
                .orElse(
                        null
                );
    }

    private boolean hasNextJoinedEventInPeriod(
            List<TeamMemberHistory> histories,
            TeamMemberHistory current,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return histories.stream()
                .filter(history -> !history.getTeamMemberId()
                        .equals(
                                current.getTeamMemberId()
                        ))
                .filter(history -> !history.getJoinedAt()
                        .isBefore(
                                current.getLeftAt()
                        ))
                .filter(history -> isMoveEvent(
                        current.getLeftAt(),
                        history.getJoinedAt()
                ))
                .anyMatch(history -> isBetween(
                        history.getJoinedAt(),
                        startAt,
                        endAt
                ));
    }

    private boolean isMoveEvent(
            LocalDateTime leftAt,
            LocalDateTime joinedAt
    ) {
        if (leftAt == null
                || joinedAt == null
                || joinedAt.isBefore(leftAt)) {
            return false;
        }

        return Duration.between(
                        leftAt,
                        joinedAt
                )
                .compareTo(
                        Duration.ofMinutes(
                                MOVE_EVENT_THRESHOLD_MINUTES
                        )
                ) <= 0;
    }

    private boolean isBetween(
            LocalDateTime value,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return value != null
                && !value.isBefore(startAt)
                && !value.isAfter(endAt);
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

    private void validatePeriod(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null
                || endDate == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "조회 시작일과 종료일을 입력해주세요."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(
                    ErrorCode.TEAM_INVALID_PERIOD
            );
        }
    }
}