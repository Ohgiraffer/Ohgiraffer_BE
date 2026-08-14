package com.ohgiraffer.team.infrastructure.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserTeamHistoryProjection {

    Long getTeamId();

    String getTeamName();

    LocalDateTime getJoinedAt();

    LocalDateTime getLeftAt();

    LocalDate getPeriodEndDate();
}