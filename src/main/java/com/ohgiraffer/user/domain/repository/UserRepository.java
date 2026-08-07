package com.ohgiraffer.user.domain.repository;

import com.ohgiraffer.user.domain.model.User;
import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;

import java.util.Optional;
import java.util.List;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    Optional<User> findById(Long userId);

    List<User> findByIdIn(List<Long> userIds);

    Optional<Long> findBootcampIdByUserId(Long userId);

    void save(User user);
    List<User> findAllByRoleAndStatus(Role role, UserStatus status);

    List<User> findByNameContaining(String keyword);

    boolean existsByEmail(String email);

    void saveAll(List<User> users);
    Optional<Long> findBootcampIdByUserId(Long userId);

}
