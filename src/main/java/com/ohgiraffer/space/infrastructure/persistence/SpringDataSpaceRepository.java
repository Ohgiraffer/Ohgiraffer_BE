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

    /*
     * 공간 삭제 안전 검사에 사용합니다.
     *
     * 사용자 상태와 관계없이 해당 공간을 참조하는
     * 오늘 위치 행이 하나라도 있는지 확인합니다.
     */
    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM trainee_location tl
                    WHERE tl.space_id = :spaceId
                      AND tl.location_date = :locationDate
                    """,
            nativeQuery = true
    )
    long countAllLocationReferences(
            @Param("spaceId")
            Long spaceId,
            @Param("locationDate")
            LocalDate locationDate
    );

    /*
     * 화면 현황과 공간 정원 검사에 사용합니다.
     *
     * GET /spaces와 동일하게 ACTIVE 사용자만
     * 현재 인원으로 계산합니다.
     */
    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM trainee_location tl
                    INNER JOIN users u
                        ON u.user_id = tl.user_id
                    WHERE tl.space_id = :spaceId
                      AND tl.location_date = :locationDate
                      AND u.status = 'ACTIVE'
                    """,
            nativeQuery = true
    )
    long countActiveOccupants(
            @Param("spaceId")
            Long spaceId,
            @Param("locationDate")
            LocalDate locationDate
    );
}