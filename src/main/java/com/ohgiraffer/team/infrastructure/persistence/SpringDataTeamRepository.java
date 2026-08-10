package com.ohgiraffer.team.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataTeamRepository
        extends JpaRepository<TeamJpaEntity, Long> {

    List<TeamJpaEntity> findAllByOrderByIdAsc();

    List<TeamJpaEntity> findAllByTeamPeriodIdAndDeletedAtIsNullOrderByIdAsc(
            Long teamPeriodId
    );

    boolean existsByNameAndTeamPeriodId(
            String name,
            Long teamPeriodId
    );

    boolean existsByNameAndTeamPeriodIdAndIdNot(
            String name,
            Long teamPeriodId,
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
}