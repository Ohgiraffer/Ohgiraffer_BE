package com.ohgiraffer.team.domain.model;

public class TeamSnapshotMember {

    private final Long teamId;
    private final String teamName;
    private final Long userId;
    private final String userName;
    private final String profileImg;

    private TeamSnapshotMember(
            Long teamId,
            String teamName,
            Long userId,
            String userName,
            String profileImg
    ) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.userId = userId;
        this.userName = userName;
        this.profileImg = profileImg;
    }

    public static TeamSnapshotMember restore(
            Long teamId,
            String teamName,
            Long userId,
            String userName,
            String profileImg
    ) {
        return new TeamSnapshotMember(
                teamId,
                teamName,
                userId,
                userName,
                profileImg
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

    public String getProfileImg() {
        return profileImg;
    }
}