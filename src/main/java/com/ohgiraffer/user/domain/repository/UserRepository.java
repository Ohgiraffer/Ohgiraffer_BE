package com.ohgiraffer.user.domain.repository;

import com.ohgiraffer.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);
}
