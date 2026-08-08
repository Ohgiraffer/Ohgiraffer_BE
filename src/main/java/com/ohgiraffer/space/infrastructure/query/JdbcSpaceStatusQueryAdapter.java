package com.ohgiraffer.space.infrastructure.query;

import com.ohgiraffer.space.application.port.SpaceOccupantData;
import com.ohgiraffer.space.application.port.SpaceStatusData;
import com.ohgiraffer.space.application.port.SpaceStatusQueryPort;
import com.ohgiraffer.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcSpaceStatusQueryAdapter
        implements SpaceStatusQueryPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<SpaceStatusData> findAllSpaceStatuses() {
        String sql = """
                SELECT
                    s.space_id,
                    s.space_name,
                    COALESCE(s.max_capacity, 0) AS max_capacity,
                    u.user_id,
                    u.name AS user_name,
                    u.role AS user_role
                FROM space_reservation s
                LEFT JOIN trainee_location tl
                    ON tl.space_id = s.space_id
                    AND tl.location_date = CURRENT_DATE()
                LEFT JOIN users u
                    ON u.user_id = tl.user_id
                    AND u.status = 'ACTIVE'
                ORDER BY
                    s.space_id ASC,
                    u.name ASC,
                    u.user_id ASC
                """;

        Map<Long, MutableSpaceStatus> grouped =
                new LinkedHashMap<>();

        jdbcTemplate.query(
                sql,
                resultSet -> {
                    Long spaceId =
                            resultSet.getLong("space_id");

                    String spaceName =
                            resultSet.getString("space_name");

                    int capacity =
                            resultSet.getInt("max_capacity");

                    MutableSpaceStatus space =
                            grouped.computeIfAbsent(
                                    spaceId,
                                    ignored ->
                                            new MutableSpaceStatus(
                                                    spaceId,
                                                    spaceName,
                                                    capacity
                                            )
                            );

                    Long userId = resultSet.getObject(
                            "user_id",
                            Long.class
                    );

                    if (userId == null) {
                        return;
                    }

                    space.occupants().add(
                            new SpaceOccupantData(
                                    userId,
                                    resultSet.getString(
                                            "user_name"
                                    ),
                                    Role.valueOf(
                                            resultSet.getString(
                                                    "user_role"
                                            )
                                    )
                            )
                    );
                }
        );

        return grouped.values()
                .stream()
                .map(MutableSpaceStatus::toData)
                .toList();
    }

    private record MutableSpaceStatus(
            Long spaceId,
            String spaceName,
            int capacity,
            List<SpaceOccupantData> occupants
    ) {

        private MutableSpaceStatus(
                Long spaceId,
                String spaceName,
                int capacity
        ) {
            this(
                    spaceId,
                    spaceName,
                    capacity,
                    new ArrayList<>()
            );
        }

        private SpaceStatusData toData() {
            return new SpaceStatusData(
                    spaceId,
                    spaceName,
                    capacity,
                    occupants
            );
        }
    }
}