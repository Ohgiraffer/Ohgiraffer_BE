package com.ohgiraffer.approval.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserSignatureRepository
        extends JpaRepository<UserSignatureJpaEntity, Long> {

    Optional<UserSignatureJpaEntity> findByUserId(
            Long userId
    );

    Optional<UserSignatureJpaEntity> findByUserIdAndActiveTrue(
            Long userId
    );

    boolean existsByUserIdAndActiveTrue(
            Long userId
    );
}