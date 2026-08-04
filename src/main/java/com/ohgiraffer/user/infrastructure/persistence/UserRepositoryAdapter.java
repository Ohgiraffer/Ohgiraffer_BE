package com.ohgiraffer.user.infrastructure.persistence;

import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email)
                .map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return springDataUserRepository.findById(userId)
                .map(UserJpaEntity::toDomain);
    }
}
