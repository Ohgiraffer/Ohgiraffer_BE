package com.ohgiraffer.bootcamp.infrastructure.persistence;

import com.ohgiraffer.bootcamp.domain.model.Bootcamp;
import com.ohgiraffer.bootcamp.domain.repository.BootcampRepository;
import com.ohgiraffer.global.exception.BusinessException;
import com.ohgiraffer.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BootcampRepositoryAdapter implements BootcampRepository {

    private final SpringDataBootcampRepository springDataBootcampRepository;

    @Override
    public Bootcamp save(Bootcamp target) {
        if (target.getId() != null) {
            BootcampJpaEntity entity = springDataBootcampRepository.findById(target.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.BOOTCAMP_NOT_FOUND));
            entity.update(target.getOrgName(), target.getProName(), target.getStartDate(), target.getEndDate());
            return entity.toDomain();
        }
        BootcampJpaEntity saved = springDataBootcampRepository.save(BootcampJpaEntity.fromDomain(target));
        return saved.toDomain();
    }

    @Override
    public Optional<Bootcamp> findById(Long id) {
        return springDataBootcampRepository.findById(id)
                .map(BootcampJpaEntity::toDomain);
    }
}