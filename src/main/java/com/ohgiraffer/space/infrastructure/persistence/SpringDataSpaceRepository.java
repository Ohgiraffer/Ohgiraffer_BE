package com.ohgiraffer.space.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface SpringDataSpaceRepository
        extends JpaRepository<SpaceJpaEntity, Long> {

    boolean existsByName(
            String name
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM SpaceJpaEntity s
            WHERE s.id = :spaceId
            """)
    Optional<SpaceJpaEntity> findByIdForUpdate(
            @Param("spaceId")
            Long spaceId
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM trainee_location
                    WHERE space_id = :spaceId
                      AND location_date = :locationDate
                    """,
            nativeQuery = true
    )
    long countOccupantsBySpaceIdAndDate(
            @Param("spaceId")
            Long spaceId,
            @Param("locationDate")
            LocalDate locationDate
    );
}