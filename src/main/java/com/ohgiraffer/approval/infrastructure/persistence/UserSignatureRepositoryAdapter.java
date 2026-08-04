package com.ohgiraffer.approval.infrastructure.persistence;

import com.ohgiraffer.approval.domain.model.UserSignature;
import com.ohgiraffer.approval.domain.repository.UserSignatureRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserSignatureRepositoryAdapter
        implements UserSignatureRepository {

    private final SpringDataUserSignatureRepository repository;

    public UserSignatureRepositoryAdapter(
            SpringDataUserSignatureRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public UserSignature save(
            UserSignature userSignature
    ) {
        UserSignatureJpaEntity entity =
                UserSignatureJpaEntity.from(
                        userSignature
                );

        UserSignatureJpaEntity savedEntity =
                repository.saveAndFlush(
                        entity
                );

        return savedEntity.toDomain();
    }

    @Override
    public Optional<UserSignature> findByUserId(
            Long userId
    ) {
        return repository
                .findByUserId(
                        userId
                )
                .map(
                        UserSignatureJpaEntity::toDomain
                );
    }

    @Override
    public Optional<UserSignature> findActiveByUserId(
            Long userId
    ) {
        return repository
                .findByUserIdAndActiveTrue(
                        userId
                )
                .map(
                        UserSignatureJpaEntity::toDomain
                );
    }

    @Override
    public boolean existsActiveByUserId(
            Long userId
    ) {
        return repository
                .existsByUserIdAndActiveTrue(
                        userId
                );
    }
}