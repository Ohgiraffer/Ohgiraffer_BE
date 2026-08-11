package com.ohgiraffer.submissionbox.infrastructure.query;

import com.ohgiraffer.submissionbox.application.port.SubmissionTeamTargetPort;
import com.ohgiraffer.submissionbox.application.port.TeamSubmissionTarget;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
public class JdbcSubmissionTeamTargetAdapter
        implements SubmissionTeamTargetPort {

    /**
     * 운영진 제출 현황의 전체 대상 팀을 조회합니다.
     *
     * 삭제·해산·보관 팀과 종료된 팀 기간의 팀은
     * 현재 제출 대상에서 제외합니다.
     */
    private static final String FIND_ACTIVE_TEAMS_SQL = """
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
    private final Clock clock;

    public JdbcSubmissionTeamTargetAdapter(
            JdbcTemplate jdbcTemplate,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public List<TeamSubmissionTarget> findActiveTeams() {
        LocalDate currentDate =
                LocalDate.now(clock);

        return jdbcTemplate.query(
                FIND_ACTIVE_TEAMS_SQL,
                preparedStatement ->
                        preparedStatement.setDate(
                                1,
                                Date.valueOf(currentDate)
                        ),
                (resultSet, rowNumber) -> {
                    Long teamId =
                            resultSet.getLong(
                                    "team_id"
                            );

                    String teamName =
                            resultSet.getString(
                                    "name"
                            );

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