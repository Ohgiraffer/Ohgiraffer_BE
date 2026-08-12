package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataStudentTeamRepository
        extends JpaRepository<TeamMemberJpaEntity, Long> {

    /**
     * 기준 날짜가 포함된 팀 운영 기간에서
     * 훈련생이 소속된 팀 ID를 조회합니다.
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
                      AND :targetDate
                          BETWEEN tp.start_date
                              AND tp.end_date
                    ORDER BY tm.joined_at DESC,
                             tm.team_member_id DESC
                    """,
            nativeQuery = true
    )
    List<Long> findTeamIdsByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("targetDate") LocalDate targetDate
    );
}