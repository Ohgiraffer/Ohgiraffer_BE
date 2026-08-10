package com.ohgiraffer.team.domain.model;

public class TeamSnapshotMember {

    private final Long teamId;
    private final String teamName;
    private final Long userId;
    private final String userName;

    private TeamSnapshotMember(
            Long teamId,
            String teamName,
            Long userId,
            String userName
    ) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.userId = userId;
        this.userName = userName;
    }

    public static TeamSnapshotMember restore(
            Long teamId,
            String teamName,
            Long userId,
            String userName
    ) {
        return new TeamSnapshotMember(
                teamId,
                teamName,
                userId,
                userName
        );
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
}