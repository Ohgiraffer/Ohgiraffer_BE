package com.ohgiraffer.team.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataTeamPeriodRepository
        extends JpaRepository<TeamPeriodJpaEntity, Long> {

    List<TeamPeriodJpaEntity> findAllByArchivedAtIsNullAndDeletedAtIsNullOrderByStartDateAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tp
            FROM TeamPeriodJpaEntity tp
            WHERE tp.id = :teamPeriodId
            """)
    Optional<TeamPeriodJpaEntity> findByIdForUpdate(
            @Param("teamPeriodId") Long teamPeriodId
    );

    @Query("""
            SELECT COUNT(tp) > 0
            FROM TeamPeriodJpaEntity tp
            WHERE tp.archivedAt IS NULL
              AND tp.deletedAt IS NULL
              AND tp.startDate <= :endDate
              AND tp.endDate >= :startDate
            """)
    boolean existsVisiblePeriodOverlapping(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COUNT(tp) > 0
            FROM TeamPeriodJpaEntity tp
            WHERE tp.id <> :teamPeriodId
              AND tp.archivedAt IS NULL
              AND tp.deletedAt IS NULL
              AND tp.startDate <= :endDate
              AND tp.endDate >= :startDate
            """)
    boolean existsVisiblePeriodOverlappingAndIdNot(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("teamPeriodId") Long teamPeriodId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tp
            FROM TeamPeriodJpaEntity tp
            WHERE tp.endDate < :today
              AND tp.archivedAt IS NULL
              AND tp.deletedAt IS NULL
            ORDER BY tp.id ASC
            """)
    List<TeamPeriodJpaEntity> findArchivablePeriodsForUpdate(
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tp
            FROM TeamPeriodJpaEntity tp
            WHERE tp.archivedAt IS NOT NULL
              AND tp.archivedAt <= :deleteThreshold
              AND tp.deletedAt IS NULL
            ORDER BY tp.id ASC
            """)
    List<TeamPeriodJpaEntity> findDeletableArchivedPeriodsForUpdate(
            @Param("deleteThreshold") LocalDateTime deleteThreshold
    );
}