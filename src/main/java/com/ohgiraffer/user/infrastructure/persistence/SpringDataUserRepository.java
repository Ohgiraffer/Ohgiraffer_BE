package com.ohgiraffer.user.infrastructure.persistence;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findAllByRoleAndStatus(Role role, UserStatus status);

    @Query("SELECT u.bootcampId FROM UserJpaEntity u WHERE u.id = :userId")
    Optional<Long> findBootcampIdByUserId(@Param("userId") Long userId);
}