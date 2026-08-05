package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAttendancePolicyRepository extends JpaRepository<AttendancePolicyJpaEntity, Long> {
}