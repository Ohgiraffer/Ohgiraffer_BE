package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamMemberHistory;
import com.ohgiraffer.team.domain.model.TeamSnapshotMember;
import com.ohgiraffer.team.domain.repository.TeamHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TeamHistoryRepositoryAdapter
        implements TeamHistoryRepository {

    private final SpringDataTeamMemberRepository springDataTeamMemberRepository;

    @Override
    public List<TeamSnapshotMember> findSnapshotMembers(
            LocalDateTime snapshotAt
    ) {
        return springDataTeamMemberRepository.findSnapshotMembers(
                        snapshotAt
                )
                .stream()
                .map(this::toTeamSnapshotMember)
                .toList();
    }

    @Override
    public List<TeamMemberHistory> findHistoriesIntersectingPeriod(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return springDataTeamMemberRepository.findHistoriesIntersectingPeriod(
                        startAt,
                        endAt
                )
                .stream()
                .map(this::toTeamMemberHistory)
                .toList();
    }

    private TeamSnapshotMember toTeamSnapshotMember(
            TeamSnapshotMemberProjection projection
    ) {
        return TeamSnapshotMember.restore(
                projection.getTeamId(),
                projection.getTeamName(),
                projection.getUserId(),
                projection.getUserName()
        );
    }

    private TeamMemberHistory toTeamMemberHistory(
            TeamMemberHistoryProjection projection
    ) {
        return TeamMemberHistory.restore(
                projection.getTeamMemberId(),
                projection.getTeamId(),
                projection.getTeamName(),
                projection.getUserId(),
                projection.getUserName(),
                projection.getJoinedAt(),
                projection.getLeftAt()
        );
    }
}