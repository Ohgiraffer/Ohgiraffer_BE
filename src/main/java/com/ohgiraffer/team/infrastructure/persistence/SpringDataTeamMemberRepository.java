package com.ohgiraffer.team.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataTeamMemberRepository
        extends JpaRepository<TeamMemberViewJpaEntity, Long> {

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                tm.userId AS userId,
                u.name AS userName,
                u.email AS email,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt
            FROM TeamMemberViewJpaEntity tm
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE tm.teamId = :teamId
              AND tm.leftAt IS NULL
            ORDER BY tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberProjection> findActiveMembersByTeamId(
            @Param("teamId") Long teamId
    );

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                tm.userId AS userId,
                u.name AS userName,
                u.email AS email,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt
            FROM TeamMemberViewJpaEntity tm
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE tm.teamId IN :teamIds
              AND tm.leftAt IS NULL
            ORDER BY tm.teamId ASC, tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberProjection> findActiveMembersByTeamIds(
            @Param("teamIds") List<Long> teamIds
    );

    Optional<TeamMemberViewJpaEntity> findById(
            Long teamMemberId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tm
            FROM TeamMemberViewJpaEntity tm
            WHERE tm.id = :teamMemberId
            """)
    Optional<TeamMemberViewJpaEntity> findByIdForUpdate(
            @Param("teamMemberId") Long teamMemberId
    );

    boolean existsByUserIdAndLeftAtIsNull(
            Long userId
    );

    @Query("""
            SELECT
                u.id AS userId,
                u.name AS name,
                u.email AS email
            FROM UserJpaEntity u
            WHERE u.role = com.ohgiraffer.user.domain.model.Role.STUDENT
              AND u.status = com.ohgiraffer.user.domain.model.UserStatus.ACTIVE
              AND NOT EXISTS (
                    SELECT 1
                    FROM TeamMemberViewJpaEntity tm
                    WHERE tm.userId = u.id
                      AND tm.leftAt IS NULL
              )
            ORDER BY u.name ASC, u.id ASC
            """)
    List<UnassignedStudentProjection> findUnassignedStudents();
}