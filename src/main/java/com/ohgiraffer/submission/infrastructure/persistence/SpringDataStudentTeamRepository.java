package com.ohgiraffer.submission.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpringDataStudentTeamRepository
        extends JpaRepository<TeamMemberJpaEntity, Long> {

    /**
     * 훈련생이 현재 소속된 활성 팀 ID를 조회합니다.
     *
     * 활성 팀 조건:
     * 1. 팀원 이탈 시간이 없어야 함
     * 2. 팀이 해산되지 않아야 함
     * 3. 팀이 보관되거나 삭제되지 않아야 함
     * 4. 팀 운영 기간이 보관되거나 삭제되지 않아야 함
     * 5. 애플리케이션 기준 오늘 날짜가 팀 운영 기간 안에 있어야 함
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
                      AND tm.left_at IS NULL
                      AND t.dissolved_at IS NULL
                      AND t.archived_at IS NULL
                      AND t.deleted_at IS NULL
                      AND tp.archived_at IS NULL
                      AND tp.deleted_at IS NULL
                      AND :currentDate BETWEEN tp.start_date AND tp.end_date
                    ORDER BY tm.joined_at DESC, tm.team_member_id DESC
                    """,
            nativeQuery = true
    )
    List<Long> findActiveTeamIdsByUserId(
            @Param("userId") Long userId,
            @Param("currentDate") LocalDate currentDate
    );
}