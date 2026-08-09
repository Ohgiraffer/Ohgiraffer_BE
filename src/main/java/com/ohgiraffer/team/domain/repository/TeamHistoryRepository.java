package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.domain.model.TeamMemberHistory;
import com.ohgiraffer.team.domain.model.TeamSnapshotMember;

import java.time.LocalDateTime;
import java.util.List;

public interface TeamHistoryRepository {

    List<TeamSnapshotMember> findSnapshotMembers(
            LocalDateTime snapshotAt
    );

    List<TeamMemberHistory> findHistoriesIntersectingPeriod(
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}