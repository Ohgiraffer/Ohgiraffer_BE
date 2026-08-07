package com.ohgiraffer.submissionbox.infrastructure.query;

import com.ohgiraffer.submissionbox.application.port.SubmissionTeamTargetPort;
import com.ohgiraffer.submissionbox.application.port.TeamSubmissionTarget;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcSubmissionTeamTargetAdapter
        implements SubmissionTeamTargetPort {

    private static final String FIND_ACTIVE_TEAMS_SQL = """
            SELECT
                team_id,
                name
            FROM team
            WHERE dissolved_at IS NULL
            ORDER BY name ASC, team_id ASC
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcSubmissionTeamTargetAdapter(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TeamSubmissionTarget> findActiveTeams() {
        return jdbcTemplate.query(
                FIND_ACTIVE_TEAMS_SQL,
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