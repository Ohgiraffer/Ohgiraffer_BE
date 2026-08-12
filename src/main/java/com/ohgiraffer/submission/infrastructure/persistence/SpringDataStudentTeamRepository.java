package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpringDataStudentTeamRepository
        extends JpaRepository<TeamMemberJpaEntity, Long> {

    /**
     * 기준 일시에 학생이 실제로 소속되어 있던 팀을 조회합니다.
     *
     * 1. 기준 일자가 팀 운영 기간에 포함되어야 합니다.
     * 2. 팀원이 기준 일시 이전에 가입했어야 합니다.
     * 3. 기준 일시까지 탈퇴하지 않았어야 합니다.
     */
    @Query(
            value = """
                    SELECT tm.team_id
                    FROM team_member tm
                    JOIN team t
                      ON t.team_id = tm.team_id
                    JOIN team_period tp
                      ON tp.team_period_id = t.team_period_id
                    WHERE tm.user_id = :userId
                      AND t.dissolved_at IS NULL
                      AND t.archived_at IS NULL
                      AND t.deleted_at IS NULL
                      AND tp.archived_at IS NULL
                      AND tp.deleted_at IS NULL
                      AND DATE(:targetAt)
                          BETWEEN tp.start_date
                              AND tp.end_date
                      AND tm.joined_at <= :targetAt
                      AND (
                          tm.left_at IS NULL
                          OR tm.left_at > :targetAt
                      )
                    ORDER BY tm.joined_at DESC,
                             tm.team_member_id DESC
                    """,
            nativeQuery = true
    )
    List<Long> findTeamIdsByUserIdAndDateTime(
            @Param("userId") Long userId,
            @Param("targetAt") LocalDateTime targetAt
    );
}