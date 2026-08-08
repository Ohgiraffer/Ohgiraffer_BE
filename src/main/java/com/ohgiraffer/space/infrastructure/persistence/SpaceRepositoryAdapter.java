package com.ohgiraffer.space.infrastructure.persistence;

import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import com.ohgiraffer.space.domain.model.Space;
import com.ohgiraffer.space.domain.repository.SpaceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class SpaceRepositoryAdapter
        implements SpaceRepository {

    private final SpringDataSpaceRepository repository;

    public SpaceRepositoryAdapter(
            SpringDataSpaceRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Space save(
            Space space
    ) {
        try {
            SpaceJpaEntity entity =
                    SpaceJpaEntity.from(space);

            SpaceJpaEntity savedEntity =
                    repository.saveAndFlush(entity);

            return savedEntity.toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    ErrorCode.SPACE_NAME_DUPLICATED,
                    exception
            );
        }
    }

    @Override
    public Optional<Space> findByIdForUpdate(
            Long spaceId
    ) {
        return repository.findByIdForUpdate(spaceId)
                .map(SpaceJpaEntity::toDomain);
    }

    @Override
    public boolean existsByName(
            String name
    ) {
        return repository.existsByName(name);
    }

    @Override
    public boolean hasOccupants(
            Long spaceId,
            LocalDate locationDate
    ) {
        return countOccupants(
                spaceId,
                locationDate
        ) > 0;
    }

    @Override
    public long countOccupants(
            Long spaceId,
            LocalDate locationDate
    ) {
        return repository
                .countOccupantsBySpaceIdAndDate(
                        spaceId,
                        locationDate
                );
    }

    @Override
    public void deleteById(
            Long spaceId
    ) {
        repository.deleteById(spaceId);
        repository.flush();
    }
}