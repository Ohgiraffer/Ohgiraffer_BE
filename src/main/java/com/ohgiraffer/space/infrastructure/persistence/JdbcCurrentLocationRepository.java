package com.ohgiraffer.space.infrastructure.persistence;

import com.ohgiraffer.space.domain.repository.CurrentLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcCurrentLocationRepository
        implements CurrentLocationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Long> findCurrentSpaceId(
            Long userId,
            LocalDate locationDate
    ) {
        String sql = """
                SELECT space_id
                FROM trainee_location
                WHERE user_id = ?
                  AND location_date = ?
                  AND space_id IS NOT NULL
                """;

        return jdbcTemplate.query(
                sql,
                preparedStatement -> {
                    preparedStatement.setLong(1, userId);
                    preparedStatement.setDate(
                            2,
                            Date.valueOf(locationDate)
                    );
                },
                resultSet -> {
                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(
                            resultSet.getLong("space_id")
                    );
                }
        );
    }

    @Override
    public void saveLocation(
            Long userId,
            Long spaceId,
            LocalDate locationDate
    ) {
        String sql = """
                INSERT INTO trainee_location (
                    user_id,
                    space_id,
                    location_date
                )
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    space_id = VALUES(space_id),
                    location_date = VALUES(location_date)
                """;

        jdbcTemplate.update(
                sql,
                userId,
                spaceId,
                Date.valueOf(locationDate)
        );
    }

    @Override
    public void clearLocation(
            Long userId
    ) {
        String sql = """
                UPDATE trainee_location
                SET space_id = NULL,
                    location_date = NULL
                WHERE user_id = ?
                """;

        jdbcTemplate.update(
                sql,
                userId
        );
    }

    @Override
    public void clearAllLocations() {
        String sql = """
                UPDATE trainee_location
                SET space_id = NULL,
                    location_date = NULL
                WHERE space_id IS NOT NULL
                   OR location_date IS NOT NULL
                """;

        jdbcTemplate.update(sql);
    }

    @Override
    public void clearExpiredLocations(
            LocalDate today
    ) {
        String sql = """
                UPDATE trainee_location
                SET space_id = NULL,
                    location_date = NULL
                WHERE space_id IS NOT NULL
                  AND (
                      location_date IS NULL
                      OR location_date < ?
                  )
                """;

        jdbcTemplate.update(
                sql,
                Date.valueOf(today)
        );
    }
}