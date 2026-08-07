package com.ohgiraffer.team.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    boolean existsByTeamIdAndUserIdAndLeftAtIsNull(
            Long teamId,
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