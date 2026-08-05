package com.ohgiraffer.user.infrastructure.persistence;

import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.repository.UserRepository;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


import java.util.List;
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

    
    @Override
    public List<User> findAllByRoleAndStatus(Role role, UserStatus status) {
        return springDataUserRepository
                .findAllByRoleAndStatus(role, status)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }
}
