package com.ohgiraffer.team.infrastructure.persistence;

import java.time.LocalDateTime;

public interface TeamMemberHistoryProjection {

    Long getTeamMemberId();

    Long getTeamId();

    String getTeamName();

    Long getUserId();

    String getUserName();

    String getProfileImg();

    LocalDateTime getJoinedAt();

    LocalDateTime getLeftAt();
}