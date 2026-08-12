package com.ohgiraffer.submissionbox.infrastructure.query;

import com.ohgiraffer.submissionbox.application.port.SubmissionTeamTargetPort;
import com.ohgiraffer.submissionbox.application.port.TeamSubmissionTarget;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class JdbcSubmissionTeamTargetAdapter
        implements SubmissionTeamTargetPort {

    /**
     * 제출함 시작일이 포함된 팀 운영 기간의 팀을 조회합니다.
     *
     * 팀 자체의 임의 날짜가 아니라 팀 관리 도메인의
     * team_period.start_date, team_period.end_date를 기준으로 합니다.
     */
    private static final String FIND_TEAMS_BY_TARGET_DATE_SQL = """
            SELECT
                t.team_id,
                t.name
            FROM team t
            JOIN team_period tp
              ON tp.team_period_id = t.team_period_id
            WHERE t.dissolved_at IS NULL
              AND t.archived_at IS NULL
              AND t.deleted_at IS NULL
              AND tp.archived_at IS NULL
              AND tp.deleted_at IS NULL
              AND ? BETWEEN tp.start_date AND tp.end_date
            ORDER BY t.name ASC, t.team_id ASC
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubmissionTeamTargetAdapter(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TeamSubmissionTarget> findTeamsByTargetDate(
            LocalDate targetDate
    ) {
        if (targetDate == null) {
            throw new IllegalArgumentException(
                    "팀 제출 대상 조회 기준 날짜는 필수입니다."
            );
        }

        return jdbcTemplate.query(
                FIND_TEAMS_BY_TARGET_DATE_SQL,
                preparedStatement ->
                        preparedStatement.setDate(
                                1,
                                Date.valueOf(targetDate)
                        ),
                (resultSet, rowNumber) -> {
                    Long teamId =
                            resultSet.getLong("team_id");

                    String teamName =
                            resultSet.getString("name");

                    if (teamName == null
                            || teamName.isBlank()) {
                        teamName = "팀 " + teamId;
                    }

                    return new TeamSubmissionTarget(
                            teamId,
                            teamName.trim()
                    );
                }
        );
    }
}