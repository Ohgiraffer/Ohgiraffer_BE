package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAttendancePolicyRepository extends JpaRepository<AttendancePolicyJpaEntity, Long> {
    Optional<AttendancePolicyJpaEntity> findByBootcampId(Long bootcampId);

}