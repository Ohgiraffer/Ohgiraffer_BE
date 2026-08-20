package com.ohgiraffer.space.domain.repository;

import java.time.LocalDate;
import java.util.Optional;

public interface CurrentLocationRepository {

    Optional<Long> findCurrentSpaceId(
            Long userId,
            LocalDate locationDate
    );

    void saveLocation(
            Long userId,
            Long spaceId,
            LocalDate locationDate
    );

    void clearLocation(
            Long userId
    );

    void clearAllLocations();

    void clearExpiredLocations(
            LocalDate today
    );
}