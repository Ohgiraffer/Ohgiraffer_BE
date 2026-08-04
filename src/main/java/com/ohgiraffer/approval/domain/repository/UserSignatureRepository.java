package com.ohgiraffer.approval.domain.repository;

import com.ohgiraffer.approval.domain.model.signature.UserSignature;

import java.util.Optional;

public interface UserSignatureRepository {

    UserSignature save(
            UserSignature userSignature
    );

    Optional<UserSignature> findByUserId(
            Long userId
    );

    Optional<UserSignature> findActiveByUserId(
            Long userId
    );

    boolean existsActiveByUserId(
            Long userId
    );
}