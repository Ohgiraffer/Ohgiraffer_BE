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
    public List<User> findByIdIn(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {return List.of();}

        return springDataUserRepository.findByIdIn(userIds)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Long> findBootcampIdByUserId(Long userId) {
        return springDataUserRepository.findBootcampIdByUserId(
                userId
        );
    }

    @Override
    public List<Long> findIdsByBootcampIdAndRole(Long bootcampId, Role role) {
        return springDataUserRepository.findIdsByBootcampIdAndRole( bootcampId,role);
    }

    @Override
    public List<User> findAllByBootcampIdAndRole(Long bootcampId, Role role) {
        return springDataUserRepository.findAllByBootcampIdAndRole(bootcampId, role)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void save(User user) {
        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        springDataUserRepository.save(entity);
    }

    @Override
    public List<User> findAllByRoleAndStatus (Role role, UserStatus status){
        return springDataUserRepository
                .findAllByRoleAndStatus(role, status)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<User> findByNameContaining(String keyword) {
        return springDataUserRepository.findByNameContaining(keyword)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public void saveAll(List<User> users) {
        List<UserJpaEntity> entities = users.stream()
                .map(UserJpaEntity::fromDomain)
                .toList();
        springDataUserRepository.saveAllAndFlush(entities);
    }

    @Override
    public List<User> findAllByRoleAndStatusAndBootcampId(Role role, UserStatus status, Long bootcampId) {
        return springDataUserRepository.findAllByRoleAndStatusAndBootcampId(role, status, bootcampId)
                .stream()
                .map(UserJpaEntity::toDomain)
                .toList();
    }

}
