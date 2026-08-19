package com.ohgiraffer.team.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
                u.profileImg AS profileImg,
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tm
            FROM TeamMemberViewJpaEntity tm
            WHERE tm.teamId = :teamId
              AND tm.leftAt IS NULL
            ORDER BY tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberViewJpaEntity> findActiveMembersByTeamIdForUpdate(
            @Param("teamId") Long teamId
    );

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                tm.userId AS userId,
                u.name AS userName,
                u.email AS email,
                u.profileImg AS profileImg,
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

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                tm.userId AS userId,
                u.name AS userName,
                u.email AS email,
                u.profileImg AS profileImg,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt
            FROM TeamMemberViewJpaEntity tm
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE tm.teamId IN :teamIds
              AND tm.joinedAt < :snapshotAt
              AND (tm.leftAt IS NULL OR tm.leftAt >= :snapshotAt)
            ORDER BY tm.teamId ASC, u.name ASC, u.id ASC
            """)
    List<TeamMemberProjection> findMembersByTeamIdsAt(
            @Param("teamIds") List<Long> teamIds,
            @Param("snapshotAt") LocalDateTime snapshotAt
    );

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                tm.userId AS userId,
                u.name AS userName,
                u.email AS email,
                u.profileImg AS profileImg,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt
            FROM TeamMemberViewJpaEntity tm
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE tm.leftAt IS NULL
            ORDER BY tm.teamId ASC, tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberProjection> findActiveMembers();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tm
            FROM TeamMemberViewJpaEntity tm
            WHERE tm.leftAt IS NULL
            ORDER BY tm.teamId ASC, tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberViewJpaEntity> findActiveMembersForUpdate();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT tm
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            WHERE t.teamPeriodId = :teamPeriodId
              AND t.deletedAt IS NULL
              AND tm.leftAt IS NULL
            ORDER BY tm.teamId ASC, tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberViewJpaEntity> findActiveMembersByTeamPeriodIdForUpdate(
            @Param("teamPeriodId") Long teamPeriodId
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
                u.email AS email,
                u.profileImg AS profileImg
            FROM UserJpaEntity u
            WHERE u.role = com.ohgiraffer.user.domain.model.Role.STUDENT
              AND u.status = com.ohgiraffer.user.domain.model.UserStatus.ACTIVE
              AND NOT EXISTS (
                    SELECT 1
                    FROM TeamMemberViewJpaEntity tm
                    JOIN TeamJpaEntity t
                        ON t.id = tm.teamId
                    WHERE tm.userId = u.id
                      AND t.teamPeriodId = :teamPeriodId
                      AND t.deletedAt IS NULL
                      AND tm.leftAt IS NULL
              )
            ORDER BY u.name ASC, u.id ASC
            """)
    List<UnassignedStudentProjection> findUnassignedStudents(
            @Param("teamPeriodId") Long teamPeriodId
    );

    @Query("""
            SELECT
                tm.teamId AS teamId,
                t.name AS teamName,
                tm.userId AS userId,
                u.name AS userName,
                u.profileImg AS profileImg
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE t.teamPeriodId = :teamPeriodId
              AND tm.joinedAt <= :snapshotAt
              AND (tm.leftAt IS NULL OR tm.leftAt > :snapshotAt)
            ORDER BY t.id ASC, u.name ASC, u.id ASC
            """)
    List<TeamSnapshotMemberProjection> findSnapshotMembers(
            @Param("teamPeriodId") Long teamPeriodId,
            @Param("snapshotAt") LocalDateTime snapshotAt
    );

    @Query("""
            SELECT
                tm.id AS teamMemberId,
                tm.teamId AS teamId,
                t.name AS teamName,
                tm.userId AS userId,
                u.name AS userName,
                u.profileImg AS profileImg,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            JOIN UserJpaEntity u
                ON u.id = tm.userId
            WHERE t.teamPeriodId = :teamPeriodId
              AND tm.joinedAt <= :endAt
              AND (tm.leftAt IS NULL OR tm.leftAt >= :startAt)
            ORDER BY tm.userId ASC, tm.joinedAt ASC, tm.id ASC
            """)
    List<TeamMemberHistoryProjection> findHistoriesIntersectingPeriod(
            @Param("teamPeriodId") Long teamPeriodId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            SELECT
                tm.userId AS userId,
                t.name AS teamName
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            WHERE tm.userId IN :userIds
              AND tm.leftAt IS NULL
            """)
    List<UserTeamNameProjection> findActiveTeamNamesByUserIds(
            @Param("userIds") List<Long> userIds
    );

    @Query("""
            SELECT
                t.id AS teamId,
                t.name AS teamName,
                tm.joinedAt AS joinedAt,
                tm.leftAt AS leftAt,
                p.endDate AS periodEndDate
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            JOIN TeamPeriodJpaEntity p
                ON p.id = t.teamPeriodId
            WHERE tm.userId = :userId
              AND t.deletedAt IS NULL
            ORDER BY tm.joinedAt DESC, tm.id DESC
            """)
    List<UserTeamHistoryProjection> findUserTeamHistories(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COUNT(tm) > 0
            FROM TeamMemberViewJpaEntity tm
            JOIN TeamJpaEntity t
                ON t.id = tm.teamId
            WHERE t.teamPeriodId = :teamPeriodId
              AND tm.leftAt IS NULL
            """)
    boolean existsActiveMemberByTeamPeriodId(
            @Param("teamPeriodId") Long teamPeriodId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM TeamMemberViewJpaEntity tm
            WHERE tm.teamId IN (
                SELECT t.id
                FROM TeamJpaEntity t
                WHERE t.teamPeriodId = :teamPeriodId
            )
            """)
    void deleteByTeamPeriodId(
            @Param("teamPeriodId") Long teamPeriodId
    );
}