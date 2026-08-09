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

public interface SpringDataTeamRepository
        extends JpaRepository<TeamJpaEntity, Long> {

    List<TeamJpaEntity> findAllByOrderByIdAsc();

    List<TeamJpaEntity> findAllByArchivedAtIsNullAndDeletedAtIsNullOrderByIdAsc();

    boolean existsByName(
            String name
    );

    boolean existsByNameAndIdNot(
            String name,
            Long id
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM TeamJpaEntity t
            WHERE t.id = :teamId
            """)
    Optional<TeamJpaEntity> findByIdForUpdate(
            @Param("teamId") Long teamId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM TeamJpaEntity t
            WHERE t.endDate < :today
              AND t.archivedAt IS NULL
              AND t.deletedAt IS NULL
              AND t.dissolvedAt IS NULL
            ORDER BY t.id ASC
            """)
    List<TeamJpaEntity> findArchivableTeamsForUpdate(
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM TeamJpaEntity t
            WHERE t.archivedAt IS NOT NULL
              AND t.archivedAt <= :deleteThreshold
              AND t.deletedAt IS NULL
            ORDER BY t.id ASC
            """)
    List<TeamJpaEntity> findDeletableArchivedTeamsForUpdate(
            @Param("deleteThreshold") LocalDateTime deleteThreshold
    );
}