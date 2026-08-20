package com.ohgiraffer.space.domain.repository;

import com.ohgiraffer.space.domain.model.Space;

import java.time.LocalDate;
import java.util.Optional;

public interface SpaceRepository {

    Space save(
            Space space
    );

    Optional<Space> findByIdForUpdate(
            Long spaceId
    );

    boolean existsByName(
            String name
    );

    boolean hasOccupants(
            Long spaceId,
            LocalDate locationDate
    );

    long countOccupants(
            Long spaceId,
            LocalDate locationDate
    );

    void deleteById(
            Long spaceId
    );
}