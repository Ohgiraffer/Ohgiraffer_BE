package com.ohgiraffer.team.domain.model;

import java.time.LocalDateTime;

public class TeamMemberHistory {

    private final Long teamMemberId;
    private final Long teamId;
    private final String teamName;
    private final Long userId;
    private final String userName;
    private final LocalDateTime joinedAt;
    private final LocalDateTime leftAt;

    private TeamMemberHistory(
            Long teamMemberId,
            Long teamId,
            String teamName,
            Long userId,
            String userName,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        this.teamMemberId = teamMemberId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.userId = userId;
        this.userName = userName;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public static TeamMemberHistory restore(
            Long teamMemberId,
            Long teamId,
            String teamName,
            Long userId,
            String userName,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        return new TeamMemberHistory(
                teamMemberId,
                teamId,
                teamName,
                userId,
                userName,
                joinedAt,
                leftAt
        );
    }

    public Long getTeamMemberId() {
        return teamMemberId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }
}