package com.ohgiraffer.bootcamp.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAttendancePeriodRepository extends JpaRepository<AttendancePeriodJpaEntity, Long> {
}