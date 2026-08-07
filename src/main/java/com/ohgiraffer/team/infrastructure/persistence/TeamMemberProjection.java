package com.ohgiraffer.team.infrastructure.persistence;

import java.time.LocalDateTime;

public interface TeamMemberProjection {

    Long getTeamMemberId();

    Long getTeamId();

    Long getUserId();

    String getUserName();

    String getEmail();

    LocalDateTime getJoinedAt();

    LocalDateTime getLeftAt();
}