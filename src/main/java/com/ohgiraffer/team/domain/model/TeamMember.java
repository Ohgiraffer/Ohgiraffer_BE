package com.ohgiraffer.team.domain.model;

import java.time.LocalDateTime;

public class TeamMember {

    private final Long id;
    private final Long teamId;
    private final Long userId;
    private final String userName;
    private final String email;
    private final LocalDateTime joinedAt;
    private final LocalDateTime leftAt;

    private TeamMember(
            Long id,
            Long teamId,
            Long userId,
            String userName,
            String email,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
    }

    public static TeamMember restore(
            Long id,
            Long teamId,
            Long userId,
            String userName,
            String email,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {
        return new TeamMember(
                id,
                teamId,
                userId,
                userName,
                email,
                joinedAt,
                leftAt
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLeftAt() {
        return leftAt;
    }

    public boolean isActive() {
        return leftAt == null;
    }
}