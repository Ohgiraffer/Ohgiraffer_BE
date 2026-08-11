package com.ohgiraffer.team.infrastructure.persistence;

import com.ohgiraffer.team.domain.model.TeamPeriod;
import com.ohgiraffer.team.domain.repository.TeamPeriodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamPeriodRepositoryAdapter
        implements TeamPeriodRepository {

    private final SpringDataTeamPeriodRepository springDataTeamPeriodRepository;

    @Override
    public TeamPeriod save(
            TeamPeriod teamPeriod
    ) {
        TeamPeriodJpaEntity savedEntity =
                springDataTeamPeriodRepository.saveAndFlush(
                        TeamPeriodJpaEntity.from(
                                teamPeriod
                        )
                );

        return savedEntity.toDomain();
    }

    @Override
    public List<TeamPeriod> findVisiblePeriods() {
        return springDataTeamPeriodRepository.findAllByArchivedAtIsNullAndDeletedAtIsNullOrderByStartDateAsc()
                .stream()
                .map(TeamPeriodJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<TeamPeriod> findById(
            Long teamPeriodId
    ) {
        return springDataTeamPeriodRepository.findById(
                        teamPeriodId
                )
                .map(TeamPeriodJpaEntity::toDomain);
    }

    @Override
    public Optional<TeamPeriod> findByIdForUpdate(
            Long teamPeriodId
    ) {
        return springDataTeamPeriodRepository.findByIdForUpdate(
                        teamPeriodId
                )
                .map(TeamPeriodJpaEntity::toDomain);
    }

    @Override
    public boolean existsVisiblePeriodOverlapping(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return springDataTeamPeriodRepository.existsVisiblePeriodOverlapping(
                startDate,
                endDate
        );
    }

    @Override
    public boolean existsVisiblePeriodOverlappingAndIdNot(
            LocalDate startDate,
            LocalDate endDate,
            Long teamPeriodId
    ) {
        return springDataTeamPeriodRepository.existsVisiblePeriodOverlappingAndIdNot(
                startDate,
                endDate,
                teamPeriodId
        );
    }

    @Override
    public List<TeamPeriod> findArchivablePeriodsForUpdate(
            LocalDate today
    ) {
        return springDataTeamPeriodRepository.findArchivablePeriodsForUpdate(
                        today
                )
                .stream()
                .map(TeamPeriodJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<TeamPeriod> findDeletableArchivedPeriodsForUpdate(
            LocalDateTime deleteThreshold
    ) {
        return springDataTeamPeriodRepository.findDeletableArchivedPeriodsForUpdate(
                        deleteThreshold
                )
                .stream()
                .map(TeamPeriodJpaEntity::toDomain)
                .toList();
    }
}