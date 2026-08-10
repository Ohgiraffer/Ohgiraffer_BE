package com.ohgiraffer.user.infrastructure.persistence;

import com.ohgiraffer.user.domain.model.Role;
import com.ohgiraffer.user.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findAllByRoleAndStatus(Role role, UserStatus status);

    List<UserJpaEntity> findByIdIn(List<Long> userIds);

    @Query("SELECT u.bootcampId FROM UserJpaEntity u WHERE u.id = :userId")
    Optional<Long> findBootcampIdByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserJpaEntity u SET u.bootcampId = :bootcampId WHERE u.id = :userId AND u.bootcampId IS NULL")
    int assignBootcampIfAbsent(@Param("userId") Long userId, @Param("bootcampId") Long bootcampId);

    List<UserJpaEntity> findByNameContaining(String keyword);

    List<UserJpaEntity> findAllByRoleAndStatusAndBootcampId(Role role, UserStatus status, Long bootcampId);

    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM UserJpaEntity u WHERE u.bootcampId = :bootcampId AND u.role = :role")
    List<Long> findIdsByBootcampIdAndRole(@Param("bootcampId") Long bootcampId, @Param("role") Role role);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.bootcampId = :bootcampId AND u.role = :role")
    List<UserJpaEntity> findAllByBootcampIdAndRole(@Param("bootcampId") Long bootcampId, @Param("role") Role role);

    List<UserJpaEntity> findAllByBootcampId(Long bootcampId);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE UserJpaEntity u
        SET u.status = com.ohgiraffer.user.domain.model.UserStatus.COMPLETED
        WHERE u.bootcampId = :bootcampId
          AND u.role = com.ohgiraffer.user.domain.model.Role.STUDENT
          AND u.status = com.ohgiraffer.user.domain.model.UserStatus.ACTIVE
        """)
    int completeActiveStudentsByBootcampId(@Param("bootcampId") Long bootcampId);
}