package com.ohgiraffer.team.infrastructure.persistence;

public interface TeamSnapshotMemberProjection {

    Long getTeamId();

    String getTeamName();

    Long getUserId();

    String getUserName();
}