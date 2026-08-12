package com.ohgiraffer.team.infrastructure.persistence;

import java.time.LocalDate;

public interface UserTeamHistoryProjection {

    Long getTeamId();

    String getTeamName();

    LocalDate getStartDate();

    LocalDate getEndDate();
}