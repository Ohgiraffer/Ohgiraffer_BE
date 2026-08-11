package com.ohgiraffer.team.domain.repository;

import com.ohgiraffer.team.domain.model.TeamPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamPeriodRepository {

    TeamPeriod save(
            TeamPeriod teamPeriod
    );

    List<TeamPeriod> findVisiblePeriods();

    Optional<TeamPeriod> findById(
            Long teamPeriodId
    );

    Optional<TeamPeriod> findByIdForUpdate(
            Long teamPeriodId
    );

    List<TeamPeriod> findArchivablePeriodsForUpdate(
            LocalDate today
    );

    List<TeamPeriod> findDeletableArchivedPeriodsForUpdate(
            LocalDateTime deleteThreshold
    );
}